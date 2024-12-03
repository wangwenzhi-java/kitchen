package com.wwz.kitchen.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Created by wenzhi.wang.
 * on 2024/11/17.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
/*    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许所有路径
                .allowedOrigins("http://localhost:8080")  // 允许的前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的请求方法
                .allowCredentials(true)  // 允许携带 Cookie
                .allowedHeaders("*")  // 允许的请求头
                .maxAge(3600);  // 预检请求的有效期，单位为秒
    }*/
}
