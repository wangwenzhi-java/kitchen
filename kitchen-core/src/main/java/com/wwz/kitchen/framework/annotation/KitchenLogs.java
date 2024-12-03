package com.wwz.kitchen.framework.annotation;

import com.wwz.kitchen.business.enums.PlatformEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义日志注解 方便记录
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KitchenLogs {
    /**
     * 业务信息
     */
    String value() default "";
    /**
     * 平台、服务
     */
    PlatformEnum platform() default PlatformEnum.KITCHEN;

    /**
     * 是否持久化日志
     */
    boolean save() default true;
}
