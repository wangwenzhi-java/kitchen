package com.wwz.kitchen.framework.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
/**
 * Redis 配置类
 * Created by wenzhi.wang.
 * on 2024/11/13.
 * @since 1.0
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.sentinel.master}")
    private String master; // 主节点名称

    @Value("${spring.redis.sentinel.nodes}")
    private String sentinelNodes; // Sentinel 节点，逗号分隔的字符串

    @Value("${spring.redis.password}") // 添加密码配置
    private String password;

    @Value("${spring.redis.database:1}") // 读取数据库索引，默认是 1
    private int database;

    /**
     * 自定义缓存数据时 Key 的生成器，可以根据业务需求进行调整
     *
     * @return 自定义 Key 生成器
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            // 类名 + 方法名
            sb.append(target.getClass().getName());
            sb.append(".").append(method.getName());
            for (Object obj : params) {
                sb.append(obj);
            }
            return sb.toString();
        };
    }

    /**
     * 配置 CacheManager，设置 Redis 缓存管理器
     *
     * @param factory Redis 连接工厂
     * @return CacheManager 实例
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(factory))
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1))) // 默认缓存过期时间为 1 天
                .transactionAware()
                .build();
    }

    /**
     * 配置 RedisTemplate，设置序列化方式
     *
     * @param factory Redis 连接工厂
     * @return RedisTemplate 实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 配置 Jackson 序列化
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 设置键和值的序列化方式
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.afterPropertiesSet(); // 确保属性设置完成

        return template;
    }

    /**
     * 配置 RedisConnectionFactory 以支持 Sentinel
     *
     * @return RedisConnectionFactory 实例
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        Set<String> sentinels = new HashSet<>();
        for (String node : sentinelNodes.split(",")) {
            sentinels.add(node.trim()); // 分割并去掉空格
        }

        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration(master, sentinels);
        sentinelConfig.setPassword(password); // 设置密码
        sentinelConfig.setDatabase(database); // 设置数据库索引
        return new LettuceConnectionFactory(sentinelConfig);
    }
}

