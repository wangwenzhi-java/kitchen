package com.wwz.kitchen.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wwz.kitchen.business.dto.KitchenUserModeDto;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.service.KitchenUserModeService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.annotation.KitchenLogs;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import com.wwz.kitchen.persistence.beans.KitchenUserMode;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.util.AjaxResultUtil;
import com.wwz.kitchen.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by wenzhi.wang.
 * on 2024/11/17.
 */
@RestController
@RequestMapping("/userMode")
@Slf4j
public class KitchenUserModeController {
    @Autowired
    private KitchenUserModeService kitchenUserModeService;
    @Autowired
    private KitchenUsersService kitchenUsersService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/changeMode")
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    @KitchenLogs(value = "用户修改使用模式",platform = PlatformEnum.KITCHEN)
    public String changeMode(HttpServletRequest request, @RequestBody  @Valid KitchenUserModeDto kitchenUserModeDto) {
        JSONObject res = new JSONObject();
        try {
            String tokenFromRequest = TokenUtil.getTokenFromRequest(request);
            boolean temp = jwtTokenUtil.validateToken(tokenFromRequest);
            if (!temp) {
                res.put("msg", AjaxResponseCodeEnum.USER_TOKEN_ERROR.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.USER_TOKEN_ERROR.getCode());
            }
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            KitchenUsers one = kitchenUsersService.getUserByUserName(username);
            if (one == null) {
                res.put("msg", AjaxResponseCodeEnum.UNAUTHORIZED.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.UNAUTHORIZED.getCode());
            }
            boolean update = kitchenUserModeService.updateByUidAndUserMode(one.getId(),kitchenUserModeDto.getKitchenUserMode());
            if (update) {
                res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
            }
        } catch (Exception e) {
            log.error("KitchenUserModeController===============changeMode():{}",e.getMessage());
        }
        res.put("msg",AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
        return AjaxResultUtil.response(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
    }
}
