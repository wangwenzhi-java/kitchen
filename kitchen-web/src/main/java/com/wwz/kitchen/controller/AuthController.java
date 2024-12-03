package com.wwz.kitchen.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwz.kitchen.business.dto.JwtResponseDto;
import com.wwz.kitchen.business.dto.KitchenUsersLoginDTO;
import com.wwz.kitchen.business.dto.KitchenUsersRegistryDTO;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.business.enums.KitchenUserModeEnum;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.service.KitchenUserModeService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.annotation.KitchenLogs;
import com.wwz.kitchen.framework.exception.DailyLimitExceededException;
import com.wwz.kitchen.framework.exception.KitchenException;
import com.wwz.kitchen.framework.mail.EmailVerificationService;
import com.wwz.kitchen.framework.qinqiu.QiniuService;
import com.wwz.kitchen.framework.security.CustomUserDetailsService;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import com.wwz.kitchen.persistence.beans.KitchenUserMode;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.util.AjaxResultUtil;
import com.wwz.kitchen.util.EmailUtil;
import com.wwz.kitchen.util.TokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController extends BaseController{
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private KitchenUsersService  kitchenUsersService;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private QiniuService qiniuService;
    @Autowired
    private KitchenUserModeService kitchenUserModeService;
    //邮件验证码请求接口
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    @PostMapping("/sendVerificationCode")
    @KitchenLogs(value = "用户发送邮件验证码",platform = PlatformEnum.KITCHEN)
    public String sendVerificationCode(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
        String email = requestMap.get("email");
        JSONObject result = new JSONObject();
        if (!EmailUtil.isEmail(email)){
            result.put("msg",AjaxResponseCodeEnum.EMAIL_FORMAT_ERROR.getMessage());
            return AjaxResultUtil.response(result, AjaxResponseCodeEnum.EMAIL_FORMAT_ERROR.getCode());
        }
        KitchenUsers one = kitchenUsersService.getUserByEmail(email);
        if (null != one) {
            result.put("msg",AjaxResponseCodeEnum.EMAIL_EXIST_ERROR.getMessage());
            return AjaxResultUtil.response(result, AjaxResponseCodeEnum.EMAIL_EXIST_ERROR.getCode());
        }
        try {
            emailVerificationService.sendVerificationCode(email);
            result.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(result, AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (DailyLimitExceededException e) { //验证码发送已经达到 日上线
            result.put("msg",AjaxResponseCodeEnum.EMAIL_LIMIT_ERROR.getMessage());
            return AjaxResultUtil.response(result, AjaxResponseCodeEnum.EMAIL_LIMIT_ERROR.getCode());
        } catch (KitchenException e) {
            result.put("msg",AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
            return AjaxResultUtil.response(result, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
        }
    }

    //用户注册接口
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    @PostMapping("/register")
    @KitchenLogs(value = "用户注册",platform = PlatformEnum.KITCHEN)
    public String register(@RequestBody @Valid KitchenUsersRegistryDTO kitchenUsersRegistryDTO, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        String email = kitchenUsersRegistryDTO.getEmail();
        KitchenUsers user = kitchenUsersService.getUserByEmail(email);
        if (user != null ){ //邮箱已注册
            res.put("msg",AjaxResponseCodeEnum.EMAIL_EXIST_ERROR.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.EMAIL_EXIST_ERROR.getCode());
        }
        String code = kitchenUsersRegistryDTO.getCode();
        String username = kitchenUsersRegistryDTO.getUsername();
        KitchenUsers user1 = kitchenUsersService.getUserByUserName(username);
        if (user1 !=null ){ //用户名已存在
            res.put("msg",AjaxResponseCodeEnum.USERNAME_EXIST_ERROR.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.USERNAME_EXIST_ERROR.getCode());
        }
        if (!emailVerificationService.verifyCode(email,code)) {//验证码验证不通过  过期或验证码与邮箱不符
            res.put("msg",AjaxResponseCodeEnum.EMAIL_CODE_ERROR.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.EMAIL_CODE_ERROR.getCode());
        }
        if (!kitchenUsersRegistryDTO.getPassword().equals(kitchenUsersRegistryDTO.getRePassword())) {//二次校验密码错误
            res.put("msg",AjaxResponseCodeEnum.PASSWORD_VALIDATION_ERROR.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.PASSWORD_VALIDATION_ERROR.getCode());
        }

        // 使用 BCryptPasswordEncoder 加密密码
        String encodedPassword = passwordEncoder.encode(kitchenUsersRegistryDTO.getPassword());
        kitchenUsersRegistryDTO.setPassword(encodedPassword);
        kitchenUsersRegistryDTO.setRePassword(encodedPassword);//这句没啥用 但是不想删
        int registryId = kitchenUsersService.registry(kitchenUsersRegistryDTO);//持久化
        if (registryId <= 0){
            res.put("msg",AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
        }
        //初始化用户模式 0默认模式 1.自定义模式
        KitchenUserMode kitchenUserMode = new KitchenUserMode();
        kitchenUserMode.setUid(registryId);
        kitchenUserMode.setUserMode(KitchenUserModeEnum.DEFAULT.getCode());//初始化时为默认模式
        boolean initMode = kitchenUserModeService.save(kitchenUserMode);
        if (!initMode) {
            res.put("msg",AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
        }
        res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
        return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
    }

    //用户登录
    @PostMapping("/login")
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    @KitchenLogs(value = "用户登录",platform = PlatformEnum.KITCHEN)
    public String login(@RequestBody @Valid KitchenUsersLoginDTO kitchenUsersLoginDTO) {
        JSONObject res = new JSONObject();
        try {
            // 获取前端传过来的用户名和密码
            String username = kitchenUsersLoginDTO.getUsername();
            String password = kitchenUsersLoginDTO.getPassword();
            log.info("username正在登录=======>{}",username);
            log.info("password=======>{}",password);
            // 使用 AuthenticationManager 进行认证，确保用户名和密码正确
            Authentication authentication = authenticationManager.authenticate( //认证的过程是springsecurity的核心逻辑
               new UsernamePasswordAuthenticationToken(username, password)      // 如果不返回authentication会抛出异常 因此我们在处理登录逻辑时不用关心返回值，只需进行异常处理
            );
            log.info("authentication=======>{}",authentication);
            // 生成 JWT Token
            String token = jwtTokenUtil.generateToken(username);
            // 返回生成的 token 和消息
            res.put("token", token);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (BadCredentialsException e){
            // 捕获错误的用户名或密码
            res.put("msg", e.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.BAD_REQUEST.getCode());
        } catch (UsernameNotFoundException e) {
            // 捕获用户不存在
            res.put("msg", e.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.USERNAME_NOT_EXIST_ERROR.getCode());
        } catch (LockedException e) {
            // 用户被锁定的情况
            res.put("msg", "账户已被锁定");
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.FORBIDDEN.getCode());
        } catch (AccountExpiredException e) {
            // 账户过期的情况
            res.put("msg", "账户已过期");
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.FORBIDDEN.getCode());
        } catch (DisabledException e) {
            // 用户账户被禁用的情况
            res.put("msg", "账户已被禁用");
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.FORBIDDEN.getCode());
        } catch (Exception e) {
            log.error(e.getMessage());
            // 处理其他未知异常
            res.put("msg", "登录失败，请稍后再试");
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
        }
    }

    //登录状态验证 初始化用户数据
    @PostMapping("/validateToken")
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    @KitchenLogs(value = "验证token，登录状态验证",platform = PlatformEnum.KITCHEN)
    public String validateToken(HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try{
            String tokenFromRequest = TokenUtil.getTokenFromRequest(request);
            boolean temp = jwtTokenUtil.validateToken(tokenFromRequest);
            if (!temp) {
                res.put("msg",AjaxResponseCodeEnum.USER_TOKEN_ERROR.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.USER_TOKEN_ERROR.getCode());
            }
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            /*加入用户信息*/
            /*======user=========================================================================================================================================*/
            KitchenUsers one = kitchenUsersService.getUserByUserName(username);
            Integer uid = 0;
            String email = "";
            String nickname = "";
            String avatar = "";
            String createDate = "";
            if (one != null) {
                email = one.getEmail() == null ? "" : one.getEmail();
                nickname = one.getNickname() == null ? "" : one.getNickname();
                avatar = one.getAvatar() == null ? "" : one.getAvatar();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  // 只显示日期
                createDate = dateFormat.format(one.getCreateTime());
                uid = one.getId();
            }
            res.put("id",uid);
            res.put("username",username);
            res.put("nickname",nickname);
            res.put("avatar",avatar);
            res.put("email",email);
            res.put("createDate",createDate);
            /*======userMode=========================================================================================================================================*/
            KitchenUserMode kitchenUserMode = kitchenUserModeService.getUserModeByUid(uid);
            Integer userMode = 0;
            String userModeStr = "";
            if (kitchenUserMode != null) {
                userMode = kitchenUserMode.getUserMode();
                userModeStr = KitchenUserModeEnum.fromCode(kitchenUserMode.getUserMode()).getMessage();
            }
            res.put("userMode",userMode);
            res.put("userModeStr",userModeStr);
            /*加入用户信息*/
            res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
        }catch (ExpiredJwtException e) {
            // Token 已过期
            log.info("Token is expired");
        } catch (SignatureException e) {
            // Token 签名不匹配
            log.info("Invalid Token Signature");
        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效或格式错误
            log.info("Invalid Token");
        } catch (KitchenException e) {
            log.info(e.getMessage());
        }
        res.put("msg",AjaxResponseCodeEnum.UNAUTHORIZED.getMessage());
        return AjaxResultUtil.response(res, AjaxResponseCodeEnum.UNAUTHORIZED.getCode());
    }

    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    @PostMapping("/uploadAvatar")
    @KitchenLogs(value = "用户上传头像到七牛云", platform = PlatformEnum.KITCHEN)
    public String uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        // 验证Token有效性
        String tokenFromRequest = TokenUtil.getTokenFromRequest(request);
        if (!isValidToken(tokenFromRequest)) {
            return buildErrorResponse(res, AjaxResponseCodeEnum.USER_TOKEN_ERROR);
        }
        try {
            String url = qiniuService.uploadAvatar(file, request);
            if (StringUtils.isEmpty(url)) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.USER_AVATAR_ERROR);
            }
            // 更新用户头像
            return updateUserAvatar(res, url);
        } catch (IOException e) {
            log.error("上传头像异常", e);
            return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
        }
    }

    // 校验Token
    public boolean isValidToken(String token) {
        return jwtTokenUtil.validateToken(token);
    }

    // 构建错误响应
    private String buildErrorResponse(JSONObject res, AjaxResponseCodeEnum errorCode) {
        res.put("msg", errorCode.getMessage());
        return AjaxResultUtil.response(res, errorCode.getCode());
    }
    // 更新用户头像
    private String updateUserAvatar(JSONObject res, String avatarUrl) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        KitchenUsers user = kitchenUsersService.getUserByUserName(username);
        if (user == null) {
            return buildErrorResponse(res, AjaxResponseCodeEnum.USERNAME_NOT_EXIST_ERROR);
        }
        user.setAvatar(avatarUrl);
        boolean updateSuccess = kitchenUsersService.updateByUser(user);
        if (updateSuccess) {
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            res.put("avatar", avatarUrl);
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
        }
        return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    // 刷新 JWT 令牌
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        // 从请求头中获取当前的 JWT
        String token = TokenUtil.getTokenFromRequest(request);

        // 如果没有 JWT 或者 JWT 无效
        if (token == null || !jwtTokenUtil.validateToken(token, customUserDetailsService.loadUserByUsername(jwtTokenUtil.getUsernameFromToken(token)).getUsername())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效或过期的令牌");
        }

        // 如果 JWT 已经过期
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("令牌已过期");
        }

        // 如果 JWT 接近过期，刷新令牌
        if (jwtTokenUtil.isTokenAlmostExpired(token)) {
            // 从用户名中获取用户详细信息
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // 生成新的 JWT
            String newToken = jwtTokenUtil.generateToken(userDetails.getUsername());

            // 返回新的 JWT
            return ResponseEntity.ok(new JwtResponseDto(newToken));
        }
        return ResponseEntity.ok("当前令牌无需刷新");
    }

}
