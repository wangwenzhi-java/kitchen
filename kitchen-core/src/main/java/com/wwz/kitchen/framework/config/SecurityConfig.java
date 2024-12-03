package com.wwz.kitchen.framework.config;

import com.wwz.kitchen.framework.filter.JwtAuthenticationFilter;
import com.wwz.kitchen.framework.security.CustomUserDetailsService;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 *  由于是前后端分离项目 所以
 *  配置 Spring Security 以支持 REST API
 * Created by wenzhi.wang.
 * on 2024/11/14.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter  {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenUtil, customUserDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()  // 启用 CORS 配置
                .csrf().disable()  // 禁用 CSRF 防护，适用于 RESTful API
                .authorizeRequests()
                // 配置开放的 API 路径
                .mvcMatchers( "/auth/**","/userMode/**","/category/**","/menu/**","/pick/**","/days/**","/ws/**","/friend/**").permitAll()  //允许这些路径无需认证
                .mvcMatchers().authenticated()  // 需要登录才能访问的 API
                .anyRequest().authenticated()  // 其他路径需要认证
                .and()
                // 在 UsernamePasswordAuthenticationFilter 前添加自定义的 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    //密码加密规则
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 我也是第一次用 记录一下它的作用
     * AuthenticationManager 是 Spring Security 的核心组件之一，
     * 负责验证用户身份。它的主要作用是接收认证请求（Authentication 对象），
     * 根据配置的认证机制（如用户名密码验证、OAuth2、LDAP 等）进行验证，
     * 并返回一个已认证的 Authentication 对象。如果认证失败，会抛出相应的异常。
     * @param configuration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
