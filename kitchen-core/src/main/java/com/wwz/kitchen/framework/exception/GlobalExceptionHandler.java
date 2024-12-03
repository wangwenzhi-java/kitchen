package com.wwz.kitchen.framework.exception;

import com.alibaba.fastjson.JSONObject;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.util.AjaxResultUtil;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理器
 * Created by wenzhi.wang.
 * on 2024/11/29.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    //未登录异常统一处理
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException ex) {
        JSONObject res = new JSONObject();
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
    }
}
