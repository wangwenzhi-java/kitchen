package com.wwz.kitchen.framework.cachekey;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 缓存键生成策略接口
 * Created by wenzhi.wang.
 * on 2024/11/28.
 */
public interface CacheKeyGenerator {
    String generateKey(ProceedingJoinPoint pjp, Object[] args);
}
