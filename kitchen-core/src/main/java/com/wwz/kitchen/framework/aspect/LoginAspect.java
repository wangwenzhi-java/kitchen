package com.wwz.kitchen.framework.aspect;

import com.wwz.kitchen.framework.exception.UnauthorizedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Created by wenzhi.wang.
 * on 2024/11/28.
 */
@Aspect
@Component
public class LoginAspect {

    @Pointcut("@annotation(com.wwz.kitchen.framework.annotation.RequiresLogin)")
    public void requiresLoginMethods() {}

    @Before("requiresLoginMethods()")
    public void checkLogin(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("用户未登录");
        }
    }
}
