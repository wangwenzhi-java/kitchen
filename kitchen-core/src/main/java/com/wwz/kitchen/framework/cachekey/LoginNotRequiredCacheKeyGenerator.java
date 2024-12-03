package com.wwz.kitchen.framework.cachekey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 *
 * 需要登录的情况下
 * 缓存键生成策略
 * Created by wenzhi.wang.
 * on 2024/11/28.
 */
@Component
public class LoginNotRequiredCacheKeyGenerator implements CacheKeyGenerator {

    @Override
    public String generateKey(ProceedingJoinPoint pjp, Object[] args) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String methodName = signature.getName();

        // 使用 StringBuilder 来拼接缓存 key
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(methodName).append("-");

        // 遍历方法参数并拼接到缓存 key 中
        for (Object arg : args) {
            if (arg != null) {
                // 如果是集合或Map类型，使用 hashCode() 或者 JSON 序列化后进行哈希处理
                if (arg instanceof Collection || arg instanceof Map) {
                    keyBuilder.append("-").append(hashObject(arg));
                } else if (arg instanceof String) {
                    keyBuilder.append("-").append((String) arg);  // 字符串直接拼接
                } else if (arg instanceof Integer || arg instanceof Long || arg instanceof Double || arg instanceof Boolean) {
                    keyBuilder.append("-").append(arg.toString());  // 基本数据类型直接拼接
                } else {
                    keyBuilder.append("-").append(hashObject(arg)); // 对其他对象进行哈希处理
                }
            } else {
                keyBuilder.append("-null");
            }
        }
        // 返回生成的缓存键
        return keyBuilder.toString();
    }

    /**
     * 根据方法名和参数列表获取key
     * @param methodName 方法名
     * @param args 方法参数
     * @return 缓存的 Key
     */
    public String getGenerateKey(String methodName, Object[] args) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(methodName).append("-");

        // 遍历参数并拼接
        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof Collection || arg instanceof Map) {
                    keyBuilder.append("-").append(hashObject(arg));
                } else {
                    keyBuilder.append("-").append(arg.toString());
                }
            } else {
                keyBuilder.append("-null");
            }
        }
        return keyBuilder.toString();
    }

    /**
     * 对复杂对象（如 List, Map, 自定义对象）进行哈希处理，避免缓存键过长
     * @param obj 对象
     * @return 哈希值字符串
     */
    private String hashObject(Object obj) {
        // 使用 JSON 序列化对象，再进行哈希处理
        try {
            String json = new ObjectMapper().writeValueAsString(obj);  // 将对象转为 JSON 字符串
            return Integer.toHexString(json.hashCode());  // 返回哈希值
        } catch (JsonProcessingException e) {
            // 如果序列化失败，返回对象的 hashCode
            return Integer.toHexString(obj.hashCode());
        }
    }
}
