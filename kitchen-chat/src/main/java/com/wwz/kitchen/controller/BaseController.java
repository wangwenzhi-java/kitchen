package com.wwz.kitchen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by wenzhi.wang.
 * on 2024/11/19.
 */
public class BaseController{
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    public KitchenUsersService kitchenUsersService;

    public Integer getUid(HttpServletRequest request) {
        // 验证Token有效性
        String tokenFromRequest = TokenUtil.getTokenFromRequest(request);
        if (!jwtTokenUtil.validateToken(tokenFromRequest)) {
            return 0;
        }
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (null == username || "anonymousUser".equals(username) || "".equals(username) || "null".equals(username)) {
            return 0;
        }
        QueryWrapper<KitchenUsers> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        KitchenUsers user = kitchenUsersService.getOne(queryWrapper);
        if (user == null) {
            return 0;
        }
        return user.getId();
    }
}
