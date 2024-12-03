package com.wwz.kitchen.framework.exception;

/**
 * 验证码超日上线异常处理
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
public class DailyLimitExceededException extends RuntimeException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}
