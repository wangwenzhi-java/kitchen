package com.wwz.kitchen.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created by wenzhi.wang.
 * on 2024/11/28.
 */
public class SecurityUtil {
    public static String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取当前用户名
        String username = authentication.getName();
        if (null == username || "anonymousUser".equals(username) || "".equals(username) || "null".equals(username)) {
            return null;
        }
        return username;
    }
}
