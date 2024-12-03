package com.wwz.kitchen.framework.aspect;

import com.alibaba.fastjson.JSONObject;
import com.wwz.kitchen.business.dto.LogForRabbitDTO;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.service.KitchenLogService;
import com.wwz.kitchen.framework.annotation.KitchenLogs;

import com.wwz.kitchen.util.RabbitMqUtil;
import com.wwz.kitchen.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * AOP 自定义记录日志
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@Slf4j
@Aspect
@Component
public class KitchenLogAspect {

    @Autowired
    private RabbitMqUtil rabbitMqUtil;

    private static final String EXCHANGE_NAME = "log_direct_exchange";
    private static final String ROUTING_KEY = "logSave";

    @Pointcut(value = "@annotation(com.wwz.kitchen.framework.annotation.KitchenLogs)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object writeLog(ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        try {
            handle(point);
        } catch (Exception e) {
            log.error("日志记录时发生错误!", e);
        } finally {
            //保证执行业务
            result = point.proceed();
        }
        return result;
    }

    private void handle(ProceedingJoinPoint point) throws Exception {
        Method currentMethod = AspectUtil.INSTANCE.getMethod(point);
        //获取操作名称
        KitchenLogs annotation = currentMethod.getAnnotation(KitchenLogs.class);
        boolean save = annotation.save();
        PlatformEnum platform = annotation.platform();
        String bussinessName = AspectUtil.INSTANCE.parseParams(point.getArgs(), annotation.value());
        String ua = RequestUtil.getUa();

        log.info("{} | {} - {} {} - {}", bussinessName, RequestUtil.getIp(), RequestUtil.getMethod(), RequestUtil.getRequestUrl(), ua);
        if (!save) {
            return;
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = RequestUtil.getIp();
        String referer = RequestUtil.getReferer();
        String requestUrl = RequestUtil.getRequestUrl();
        String params = JSONObject.toJSONString(RequestUtil.getParametersMap());

        LogForRabbitDTO logForRabbitDTO = new LogForRabbitDTO();
        logForRabbitDTO.setPlatformEnum(platform);
        logForRabbitDTO.setBussinessName(bussinessName);
        logForRabbitDTO.setIp(ip);
        logForRabbitDTO.setReferer(referer);
        logForRabbitDTO.setRequestUrl(requestUrl);
        logForRabbitDTO.setParams(params);
        logForRabbitDTO.setBrowser(ua);
        logForRabbitDTO.setUsername(username);
        rabbitMqUtil.sendToRabbit(EXCHANGE_NAME,ROUTING_KEY,logForRabbitDTO);
    }
}
