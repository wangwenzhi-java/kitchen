package com.wwz.kitchen.util;



import com.wwz.kitchen.framework.holder.RequestResponseHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP 自定义记录日志
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
public class RequestUtil {

    public static String getParameters() {
        HttpServletRequest request = RequestResponseHolder.getRequest();
        if (null == request) {
            return null;
        }
        Enumeration<String> paraNames = request.getParameterNames();
        if (paraNames == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        while (paraNames.hasMoreElements()) {
            String paraName = paraNames.nextElement();
            sb.append("&").append(paraName).append("=").append(request.getParameter(paraName));
        }
        return sb.toString();
    }

    public static Map<String, String[]> getParametersMap() {
        HttpServletRequest request = RequestResponseHolder.getRequest();
        if (null == request) {
            return new HashMap<>();
        }
        return request.getParameterMap();
    }

    public static String getHeader(String headerName) {
        HttpServletRequest request = RequestResponseHolder.getRequest();
        if (null == request) {
            return null;
        }
        return request.getHeader(headerName);
    }

    public static String getReferer() {
        return getHeader("Referer");
    }

    public static String getUa() {
        return getHeader("User-Agent");
    }

    public static String getIp() {
        HttpServletRequest request = RequestResponseHolder.getRequest();
        if (null == request) {
            return null;
        }
        return IpUtil.getRealIp(request);
    }

    public static String getRequestUrl() {
        HttpServletRequest request = RequestResponseHolder.getRequest();
        if (null == request) {
            return null;
        }
        return request.getRequestURL().toString();
    }

    public static String getMethod() {
        HttpServletRequest request = RequestResponseHolder.getRequest();
        if (null == request) {
            return null;
        }
        return request.getMethod();
    }

    public static boolean isAjax(HttpServletRequest request) {
        if (null == request) {
            request = RequestResponseHolder.getRequest();
        }
        if (null == request) {
            return false;
        }
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || request.getParameter("ajax") != null;

    }

}
