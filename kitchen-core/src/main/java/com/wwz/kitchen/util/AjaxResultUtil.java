package com.wwz.kitchen.util;


import com.alibaba.fastjson.JSONObject;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 前端请求返回结果
 * Created by wenzhi.wang.
 *  * on 2024/11/15.
 */
@Slf4j
public class AjaxResultUtil {


    /**
     * ajax请求返回结果
     *
     * @param res          存放其他的返回结果
     * @param isLogPrinted true--打印log false--不打印log
     * @param value        表示状态字段isOk的值，一般以0表示服务器内部错误、refer检验不成功、参数缺失或有误等请求不成功等情况，1表示请求正常
     * @return 请求的返回结果
     */
    public static synchronized String response(JSONObject res, boolean isLogPrinted, int value) {
        res.put("isOk", value);
        if (isLogPrinted) {
            log.info("res:{}", res.toString());
        }
        return res.toString();
    }

    /**
     * ajax请求返回结果,默认打印日志
     *
     * @param res
     * @param value
     * @return
     */
    public static synchronized String response(JSONObject res, int value) {
        return response(res, true, value);
    }

    /**
     * 返回jsonp格式：解决跨域访问问题
     *
     * @param callback
     * @param res
     * @param isLogPrinted
     * @param value
     * @return
     */
    public static synchronized String jsonpResponse(String callback, JSONObject res, boolean isLogPrinted, int value) {
        res.put("isOk", value);
        StringBuilder result = new StringBuilder();
        result.append(callback).append("(");
        result.append(res);
        result.append(")");
        if (isLogPrinted) {
            log.info("res:{}", res.toString());
        }
        return result.toString();
    }

    // 构建错误响应
    public static String buildResponse(JSONObject res, AjaxResponseCodeEnum code) {
        res.put("msg", code.getMessage());
        return response(res, code.getCode());
    }
}
