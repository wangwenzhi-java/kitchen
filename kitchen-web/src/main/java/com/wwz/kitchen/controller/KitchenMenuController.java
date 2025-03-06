package com.wwz.kitchen.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wwz.kitchen.business.dto.KitchenMenuDTO;
import com.wwz.kitchen.business.enums.*;
import com.wwz.kitchen.business.service.KitchenMenuService;
import com.wwz.kitchen.business.service.KitchenMenusShareService;
import com.wwz.kitchen.business.service.KitchenUserModeService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.annotation.KitchenLogs;
import com.wwz.kitchen.framework.qinqiu.QiniuService;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import com.wwz.kitchen.persistence.beans.KitchenMenu;
import com.wwz.kitchen.persistence.beans.KitchenMenusShare;
import com.wwz.kitchen.persistence.beans.KitchenUserMode;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.util.AjaxResultUtil;
import com.wwz.kitchen.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by wenzhi.wang.
 * on 2024/11/18.
 */
@RestController
@RequestMapping("/menu")
@Slf4j
public class KitchenMenuController extends BaseController {

    @Value("${admin.id}")
    private Integer ADMIN_ID;

    @Autowired
    private KitchenMenuService kitchenMenuService;
    @Autowired
    private QiniuService qiniuService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private KitchenUsersService kitchenUsersService;
    @Autowired
    private KitchenUserModeService kitchenUserModeService;
    @Autowired
    private KitchenMenusShareService kitchenMenusShareService;
    @PostMapping("addKitchenMenu")
    @KitchenLogs(value = "用户提交菜单或服务", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String addKitchenMenu(@RequestBody @Valid KitchenMenuDTO kitchenMenuDTO,HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            // 验证Token有效性
            String tokenFromRequest = TokenUtil.getTokenFromRequest(request);
            if (!jwtTokenUtil.validateToken(tokenFromRequest)) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.USER_TOKEN_ERROR);
            }
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            /*获取用户信息*/
            /*======user=========================================================================================================================================*/
            KitchenUsers user = kitchenUsersService.getUserByUserName(username);
            if (user == null) {
                res.put("msg",AjaxResponseCodeEnum.UNAUTHORIZED.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.UNAUTHORIZED.getCode());
            }
            KitchenMenu kitchenMenu = new KitchenMenu();
            kitchenMenu.setCid(kitchenMenuDTO.getCid());
            kitchenMenu.setTid(kitchenMenuDTO.getTid());
            kitchenMenu.setUid(user.getId());
            kitchenMenu.setPic(kitchenMenuDTO.getPic());
            kitchenMenu.setDescription(kitchenMenuDTO.getDescription());
            kitchenMenu.setTitle(kitchenMenuDTO.getTitle());
            kitchenMenu.setCookbook(kitchenMenuDTO.getCookbook());
            kitchenMenu.setStatus(StatusEnum.RELEASE.toString());
            boolean save = kitchenMenuService.saveMenu(kitchenMenu);
            if (save) {
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
            }
        } catch (Exception e) {
            log.error("KitchenMenuController====addKitchenMenu:{}",e.getMessage());
        }
        res.put("msg", AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
        return AjaxResultUtil.response(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
    }


    @PostMapping("updateKitchenMenu")
    @KitchenLogs(value = "用户提交更新菜单或服务", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String updateKitchenMenu(@RequestBody @Valid KitchenMenuDTO kitchenMenuDTO,HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            //查看菜单是否是自己的 不可编辑共享菜单
            KitchenMenu oldKitchenMenu = kitchenMenuService.getById(kitchenMenuDTO.getId());
            if (uid != oldKitchenMenu.getUid()) {//不是自己的菜单
                res.put("msg", AjaxResponseCodeEnum.OPERATION_FAILED.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.OPERATION_FAILED.getCode());
            }
            KitchenMenu kitchenMenu = new KitchenMenu();
            kitchenMenu.setId(kitchenMenuDTO.getId());
            kitchenMenu.setTid(kitchenMenuDTO.getTid());
            kitchenMenu.setCid(kitchenMenuDTO.getCid());
            kitchenMenu.setPic(kitchenMenuDTO.getPic());
            kitchenMenu.setDescription(kitchenMenuDTO.getDescription());
            kitchenMenu.setTitle(kitchenMenuDTO.getTitle());
            kitchenMenu.setCookbook(kitchenMenuDTO.getCookbook());
            kitchenMenu.setStatus(StatusEnum.RELEASE.toString());
            boolean update = kitchenMenuService.updateMenu(kitchenMenu);
            if (update) {
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
            }
        } catch (Exception e) {
            log.error("KitchenMenuController====updateKitchenMenu:{}",e.getMessage());
        }
        res.put("msg", AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
        return AjaxResultUtil.response(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
    }

    @PostMapping("uploadMenu")
    @KitchenLogs(value = "用户上传菜单到七牛云", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String uploadMenu(@RequestParam("file") MultipartFile file , HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            // 验证Token有效性
            String tokenFromRequest = TokenUtil.getTokenFromRequest(request);
            if (!jwtTokenUtil.validateToken(tokenFromRequest)) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.USER_TOKEN_ERROR);
            }
            String url = qiniuService.uploadMenu(file, request);
            if (StringUtils.isEmpty(url)) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.USER_AVATAR_ERROR);
            }
            res.put("url", url);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (IOException e) {
            log.error("上传菜单异常", e);
            return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/getKitchenMenus/{tid}")
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    @KitchenLogs(value = "根据大类获取用户菜单或服务列表",platform = PlatformEnum.KITCHEN)
    public String getKitchenMenus(HttpServletRequest request, @PathVariable Integer tid) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                res.put("msg",AjaxResponseCodeEnum.UNAUTHORIZED.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.UNAUTHORIZED.getCode());
            }
            List<KitchenMenu> list = kitchenMenuService.listByUidAndTid(uid,tid,StatusEnum.RELEASE.toString());
            res.put("menus",list);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("获取菜单异常", e);
            return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 查询自己的和所有共享的菜单
     * @param request
     * @param cid
     * @return
     */
    @GetMapping("/getKitchenMenusByCid/{cid}")
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    @KitchenLogs(value = "根据小类别获取用户菜单或服务列表",platform = PlatformEnum.KITCHEN)
    public String getKitchenMenusByCid(HttpServletRequest request, @PathVariable Integer cid) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid != 0) {
                KitchenUserMode one = kitchenUserModeService.getUserModeByUid(uid);
                if (one != null && one.getUserMode() == KitchenUserModeEnum.USER_DEFINED.getCode()) { //自定义模式
                    Set<Integer> uniqueUids = new HashSet<>();
                    List<KitchenMenusShare> sharelist = kitchenMenusShareService.listByUid(uid);
                    if (sharelist == null || sharelist.size() == 0) {//无共享
                        uniqueUids.add(uid);
                    } else { //有共享
                        // 提取所有的 ShareFromUid 和 ShareToUid，并去重
                        uniqueUids = sharelist.stream()
                                .flatMap(kitchenMenusShare -> {
                                    // 先获取 ShareFromUid 和 ShareToUid，然后将它们转换为流并合并
                                    return Stream.of(kitchenMenusShare.getShareFromUid(), kitchenMenusShare.getShareToUid());
                                })
                                .collect(Collectors.toSet()); // 将它们收集到一个 Set 中，Set 自动去重
                    }

                    List<KitchenMenu> list = kitchenMenuService.listByUidsAndCid(uniqueUids,cid);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    res.put("menus",list);
                    res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                    return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
                }
            }
            //未登录和公共模式均返回公用菜单
            QueryWrapper<KitchenMenu> queryWrapperForMenu = new QueryWrapper<>();
            queryWrapperForMenu.eq("uid",ADMIN_ID);
            queryWrapperForMenu.eq("cid",cid);
            queryWrapperForMenu.eq("status",StatusEnum.RELEASE.toString());
            queryWrapperForMenu.orderByDesc("update_time");
            List<KitchenMenu> list = kitchenMenuService.list(queryWrapperForMenu);
            res.put("menus",list);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("获取菜单异常", e);
            return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
        }
    }



    @DeleteMapping("/deleteKitchenMenu/{id}")
    @KitchenLogs(value = "用户删除菜单", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String deleteKitchenMenu(@PathVariable Integer id,HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            //查看菜单是否是自己的 不可编辑共享菜单
            KitchenMenu oldKitchenMenu = kitchenMenuService.getById(id);
            if (uid != oldKitchenMenu.getUid()) {//不是自己的菜单
                res.put("msg", AjaxResponseCodeEnum.OPERATION_FAILED.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.OPERATION_FAILED.getCode());
            }

            UpdateWrapper<KitchenMenu> updateWrapper  = new UpdateWrapper<>();
            updateWrapper.eq("id", id)   // 设置更新条件，主键为 id
                    .set("status", StatusEnum.NOT_RELEASE.toString());  // 设置需要更新的字段及值
            boolean update = kitchenMenuService.update(updateWrapper);//软删除
            //boolean remove = kitchenMenuService.removeById(id);
            if (update) {
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
            }
        } catch (Exception e) {
            log.error("获取菜单异常", e);
        }
        return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    /**
     * 根据categoryId加载一个菜单或服务 用于前台随机生成页面的使用
     * 如果登录并开启自定义 按照自定义模式加载
     * 未登录或未开启自定义模式的使用默认配置
     * @param cid
     * @param request
     * @return
     */
    @GetMapping("/randomMenuByCid/{cid}")
    @KitchenLogs(value = "用户随机获取菜单或者服务", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String randomMenuByCid(@PathVariable Integer cid, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) { //未登录或者token过期 使用默认模式
                res.put("menu",getKitchenMenuForDefault(cid,ADMIN_ID));
                res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
            KitchenUserMode kitchenUserMode = kitchenUserModeService.getUserModeByUid(uid);
            if (KitchenUserModeEnum.DEFAULT.getCode() == kitchenUserMode.getUserMode()) {//用户未开启自定义 使用默认模式
                res.put("menu",getKitchenMenuForDefault(cid,ADMIN_ID));
                res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
            /*自定义模式开启,返回自定义配置*/
            res.put("menu",getKitchenMenuForDefault(cid,uid));
            res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());

        } catch (Exception e) {
            log.error("KitchenMenuController====randomMenuByCid:{}",e.getMessage());
        }
        res.put("menu","");
        res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
        return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
    }

    @GetMapping("/randomMenus")
    @KitchenLogs(value = "用户随机获取菜单或者服务(多分类同时获取)", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String randomMenus(@RequestParam Integer[] array,HttpServletRequest request) {
        System.out.println("管理员id是："+ ADMIN_ID);
        JSONObject res = new JSONObject();
        List<KitchenMenu> list = new ArrayList<>();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {//未登录或者token过期 使用默认模式
                for (int i = 0; i < array.length ; i++) {
                    KitchenMenu kitchenMenuForDefault = getKitchenMenuForDefault(array[i], ADMIN_ID);
                    list.add(kitchenMenuForDefault);
                }
                res.put("menus",list);
                res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
            KitchenUserMode kitchenUserMode = kitchenUserModeService.getUserModeByUid(uid);
            if (KitchenUserModeEnum.DEFAULT.getCode() == kitchenUserMode.getUserMode()) {//用户未开启自定义 使用默认模式
                for (int i = 0; i < array.length ; i++) {
                    KitchenMenu kitchenMenuForDefault = getKitchenMenuForDefault(array[i], ADMIN_ID);
                    list.add(kitchenMenuForDefault);
                }
                res.put("menus",list);
                res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
            /*自定义模式开启,返回自定义配置*/
            for (int i = 0; i < array.length ; i++) {
                KitchenMenu kitchenMenuForDefault = getKitchenMenuForDefault(array[i], uid);
                list.add(kitchenMenuForDefault);
            }
            res.put("menus",list);
            res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());

        } catch (Exception e) {
            log.error("KitchenMenuController====randomMenus:{}",e.getMessage());
        }
        res.put("menus","");
        res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
        return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
    }

    private KitchenMenu getKitchenMenuForDefault(Integer cid,Integer uid) {
        QueryWrapper<KitchenMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid); // 添加条件，如：查询特定类别的菜品
        queryWrapper.eq("status", StatusEnum.RELEASE.toString()); // 添加条件，如：查询特定类别的菜品
        queryWrapper.eq("uid", uid); // 未登录模式下配置一个admin账号id 配置来源于配置文件
        queryWrapper.last("ORDER BY RAND() LIMIT 1");
        KitchenMenu one = kitchenMenuService.getOne(queryWrapper);
        return one;
    }

    // 构建错误响应
    private String buildErrorResponse(JSONObject res, AjaxResponseCodeEnum errorCode) {
        res.put("msg", errorCode.getMessage());
        return AjaxResultUtil.response(res, errorCode.getCode());
    }
}
