package com.wwz.kitchen.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求头提取JWT token
 * Created by wenzhi.wang.
 * on 2024/11/17.
 */
public class TokenUtil {
    // 从请求头中获取 JWT
    public static String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
