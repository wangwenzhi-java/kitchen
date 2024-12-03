package com.wwz.kitchen.framework.mail;

import com.wwz.kitchen.business.service.EmailService;
import com.wwz.kitchen.framework.exception.DailyLimitExceededException;
import com.wwz.kitchen.framework.exception.KitchenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@Service
public class EmailVerificationService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private EmailService emailService;

    private static final String CODE_PREFIX = "email:verification:"; // Redis Key 前缀
    private static final int CODE_EXPIRATION_TIME = 5; // 过期时间（分钟）
    private static final String CODE_SUBJECT = "老喵私房菜验证码"; // 验证码邮件主题
    private static final int MAX_DAILY_ATTEMPTS = 5; // 每天最大验证码发送次数


    // 发送验证码
    public void sendVerificationCode(String email) {
        try {
            // 检查用户当天是否已经超过了发送次数
            String attemptKey = CODE_PREFIX + "attempts:" + email;
            String attemptCountStr = redisTemplate.opsForValue().get(attemptKey);
            int attemptCount = (attemptCountStr != null) ? Integer.parseInt(attemptCountStr) : 0;

            if (attemptCount >= MAX_DAILY_ATTEMPTS) {
                throw new DailyLimitExceededException("今天验证码发送次数已达上限，请明天再试");
            }

            // 生成验证码
            String code = generateRandomCode();

            // 将验证码存储到 Redis 中，设置过期时间为 5 分钟
            redisTemplate.opsForValue().set(CODE_PREFIX + email, code, CODE_EXPIRATION_TIME, TimeUnit.MINUTES);

            // 调用邮件发送服务
            sendEmail(email, code);

            // 增加用户验证码发送次数，并设置过期时间为“到零点”时间
            setExpireTimeToMidnight(attemptKey);

            redisTemplate.opsForValue().increment(attemptKey); // 递增发送次数
        } catch (DailyLimitExceededException e) {
            throw e; // 抛出用户超限异常
        } catch (Exception e) {
            throw new KitchenException("发送验证码失败", e); // 抛出通用服务异常
        }
    }

    // 验证验证码
    public boolean verifyCode(String email, String code) {
        String redisKey = CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (email != null && !"".equals(email) && storedCode != null && storedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 生成6位验证码
        return String.valueOf(code);
    }

    private void sendEmail(String email, String code) {
        String content = "你的验证码是：" + code + ",验证码有效期5分钟！";
        // 使用邮件服务发送验证码
        emailService.sendSimpleMail(email,CODE_SUBJECT,content);
    }

    /**
     * 设置 Redis 键的过期时间为当天零点
     */
    private void setExpireTimeToMidnight(String key) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 获取当天零点的时间
        LocalDateTime midnight = now.toLocalDate().atStartOfDay();

        // 计算剩余秒数
        long secondsUntilMidnight = Duration.between(now, midnight).getSeconds();

        // 设置 Redis 键的过期时间
        redisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
    }
}
