package com.wwz.kitchen.framework.security;

import io.jsonwebtoken.*;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

/**
 * JWT 工具类
 * Created by wenzhi.wang.
 * on 2024/11/14.
 */
@Component
public class JwtTokenUtil {
    // 刷新阈值15 分钟（900000 毫秒）
    private static final long refreshThreshold = 15 * 60 * 1000;

    @Value("${jwt.secret}")
    private String secretKey;

    // 过期时间，单位为毫秒
    @Value("${jwt.expiration}")
    private Long expirationTime;

    // 从用户名生成 JWT
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username);
    }

    // 从请求中提取 JWT
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 验证 JWT 是否有效
    public boolean validateToken(String token, String username) {
        String extractedUsername = getUsernameFromToken(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // 解析 JWT 中的有效负载（claims）
    private Claims getAllClaimsFromToken(String token) {
        // 将 secretKey 字符串转换为 SecretKey
        byte[] apiKeySecretBytes = secretKey.getBytes();
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // 提取 JWT 中的单个值
    private <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.resolve(claims);
    }

    // 获取 JWT 的过期时间
    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 判断 JWT 是否过期
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    // 判断 JWT 是否接近过期（过期时间小于阈值）
    public boolean isTokenAlmostExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        long timeToExpire = expiration.getTime() - System.currentTimeMillis();
        return timeToExpire < refreshThreshold;
    }

    // 创建一个新的 JWT
    private String doGenerateToken(Map<String, Object> claims, String username) {
        // 将 secretKey 字符串转换为 SecretKey
        byte[] apiKeySecretBytes = secretKey.getBytes();
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // 将 secretKey 字符串转换为 SecretKey
            byte[] apiKeySecretBytes = secretKey.getBytes();
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());
            // 解析 Token
            Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(token);  // 如果解析失败，抛出异常，Token 无效
            // 如果没有抛出异常，表示 Token 有效
            return true;
        } catch (ExpiredJwtException e) {
            // Token 已过期
            return false;
        } catch (SignatureException e) {
            // Token 签名不匹配
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效或格式错误
            return false;
        }
    }



    // Functional interface 用来提取 claims 信息
    private interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}

