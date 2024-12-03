package com.wwz.kitchen.framework.filter;

import com.wwz.kitchen.framework.exception.KitchenException;
import com.wwz.kitchen.framework.security.CustomUserDetailsService;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 自定义 JWT 过滤器
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@WebFilter
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.customUserDetailsService = customUserDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 获取请求头中的 Authorization 信息
        String authorizationHeader = request.getHeader("Authorization");

        // 检查是否有 token 且以 "Bearer " 开头
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // 去掉 "Bearer " 部分
            try {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                // 验证 JWT 并设置认证
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtTokenUtil.validateToken(token, username)) {
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                        // 将认证信息设置到 Spring Security 上下文中
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (ExpiredJwtException e) {
                // 捕获 JWT 已过期异常
                //handleJwtException(response, "JWT token has expired", HttpServletResponse.SC_UNAUTHORIZED);
            } catch (SignatureException e) {
                // 捕获 JWT 签名无效异常
                //handleJwtException(response, "Invalid JWT signature", HttpServletResponse.SC_UNAUTHORIZED);
            } catch (MalformedJwtException e) {
                // 捕获 JWT 格式无效异常
                //handleJwtException(response, "Invalid JWT token format", HttpServletResponse.SC_BAD_REQUEST);
            } catch (JwtException e) {
                // 捕获其他所有与 JWT 相关的异常
                //handleJwtException(response, "JWT validation failed", HttpServletResponse.SC_UNAUTHORIZED);
            } catch (Exception e) {
                // 捕获所有其他异常
                //handleJwtException(response, "An error occurred during authentication", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        // 继续过滤链
        filterChain.doFilter(request, response);
    }
    // 处理 JWT 异常并设置适当的响应状态和消息
    private void handleJwtException(HttpServletResponse response, String message, int statusCode) throws IOException {
        // 记录错误信息（可以使用日志记录器）
        logger.error(message);

        // 设置响应状态
        response.setStatus(statusCode);

        // 设置响应内容（错误消息）
        response.getWriter().write(message);
        response.getWriter().flush();
    }
}
