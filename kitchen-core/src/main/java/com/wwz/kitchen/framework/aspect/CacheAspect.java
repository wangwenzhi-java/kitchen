package com.wwz.kitchen.framework.aspect;

import com.wwz.kitchen.framework.annotation.CacheKeyStrategy;
import com.wwz.kitchen.framework.cachekey.LoginNotRequiredCacheKeyGenerator;
import com.wwz.kitchen.framework.cachekey.LoginRequiredCacheKeyGenerator;
import com.wwz.kitchen.framework.property.AppProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * redis切面缓存
 * Created by wenzhi.wang.
 * on 2024/11/28.
 */
@Aspect
@Component
public class CacheAspect {
    @Autowired
    private RedisTemplate<String , Object> redisTemplate;
    @Autowired
    private LoginRequiredCacheKeyGenerator loginRequiredCacheKeyGenerator;
    @Autowired
    private LoginNotRequiredCacheKeyGenerator loginNotRequiredCacheKeyGenerator;

    @Autowired
    private AppProperties appProperties;

    @Pointcut("@annotation(com.wwz.kitchen.framework.annotation.RedisCache)") // 切入点，拦截标记了 @RedisCache() 的方法
    public void cacheableMethods() {}


    @Around("cacheableMethods()")
    public Object handleCache(ProceedingJoinPoint pjp) throws Throwable {
        //是否开启全局切面缓存（app自定义配置）
        if (!appProperties.isEnableRedisCache()) {
            return pjp.proceed();
        }

        // 获取方法参数
        Object[] args = pjp.getArgs();

        // 获取方法的注解信息
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        CacheKeyStrategy cacheKeyStrategy = method.getAnnotation(CacheKeyStrategy.class);

        // 根据注解配置的策略选择缓存键生成策略
        String prefix = cacheKeyStrategy.prefix();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix);
        stringBuilder.append(":");

        String suffix;
        if (cacheKeyStrategy != null) {
            switch (cacheKeyStrategy.value()) {
                case LOGIN_REQUIRED:
                    suffix = loginRequiredCacheKeyGenerator.generateKey(pjp, args);
                    break;
                case LOGIN_NOT_REQUIRED:
                    suffix = loginNotRequiredCacheKeyGenerator.generateKey(pjp, args);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported Cache Key Strategy");
            }
        } else {
            // 如果没有配置策略，使用没登录的简单策略
            suffix = loginNotRequiredCacheKeyGenerator.generateKey(pjp, args);
        }
        stringBuilder.append(suffix);
        String cacheKey = stringBuilder.toString();

        // 从缓存中获取数据
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            // 如果缓存命中，直接返回缓存数据
            return cachedData;
        }

        // 如果缓存未命中，执行目标方法
        Object result = pjp.proceed();

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES); // 设置过期时间为 10 分钟

        return result;
    }
}
