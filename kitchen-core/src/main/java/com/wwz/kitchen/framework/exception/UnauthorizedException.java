package com.wwz.kitchen.framework.exception;

/**
 * 未登录 未授权 未认证
 * Created by wenzhi.wang.
 * on 2024/11/28.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
