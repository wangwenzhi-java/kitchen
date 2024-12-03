package com.wwz.kitchen.framework.annotation;

import com.wwz.kitchen.business.enums.CacheKeyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识缓存键生成策略
 * Created by wenzhi.wang.
 * on 2024/11/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheKeyStrategy {
    String prefix() default "";//unique前缀（设计必须保证器唯一性，最好是业务之类的命名） 避免不同类中有同名同参数导致可以冲突倒是数据泄露
    CacheKeyType value(); // 选择缓存键生成策略
}

