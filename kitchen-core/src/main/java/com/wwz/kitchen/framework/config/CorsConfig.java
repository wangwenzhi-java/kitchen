package com.wwz.kitchen.framework.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 前后端联调时详细配置跨域规则
 * 我这里没有启用 我在controller中启用了@CrossOrigin更为简单 简单测试场景可使用@CrossOrigin
 * 如有需要可开启此配置
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */

public class CorsConfig implements WebMvcConfigurer {
/*    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置哪些路径可以被跨域请求访问
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080")  // 允许的跨域请求源，可以设置多个
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允许的请求方法
                .allowedHeaders("*")  // 允许的请求头
                .allowCredentials(true)  // 是否允许发送 cookies
                .maxAge(3600);  // 缓存跨域请求的时间（秒）
    }*/
}
