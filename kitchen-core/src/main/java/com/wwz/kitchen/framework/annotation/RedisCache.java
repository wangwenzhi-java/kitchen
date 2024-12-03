package com.wwz.kitchen.framework.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenzhi.wang.
 * on 2024/11/29.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RedisCache {
    /**
     * 业务的名称
     */
    String value() default "";

    /**
     * redis缓存的Key（默认类名-方法名-自定义key）
     */
    String key() default "";

    /**
     * 是否刷新缓存，默认false
     */
    boolean flush() default false;

    /**
     * 缓存失效时间，默认30天
     */
    long expire() default 30L;

    /**
     * 缓存时间单位，默认天
     */
    TimeUnit unit() default TimeUnit.DAYS;

    /**
     * 是否启用缓存，默认启用
     */
    boolean enable() default true;
}
