package com.wwz.kitchen.business.service.impl;

import com.wwz.kitchen.business.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by wenzhi.wang.
 * on 2024/11/15.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    //Spring Boot 提供了一个发送邮件的简单接口
    @Autowired
    private JavaMailSender mailSender;

    // 配置文件中我的邮箱
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 简单文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        //创建SimpleMailMessage对象
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件发送人
        message.setFrom(from);
        //邮件接收人
        message.setTo(to);
        //邮件主题
        message.setSubject(subject);
        //邮件内容
        message.setText(content);
        //发送邮件
        mailSender.send(message);
    }

    /**
     * html邮件
     * @param to 收件人,多个时参数形式 ："xxx@xxx.com,xxx@xxx.com,xxx@xxx.com"
     * @param subject 主题
     * @param content 内容
     */
    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        //获取MimeMessage对象
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        try {
            messageHelper = new MimeMessageHelper(message, true);
            //邮件发送人
            messageHelper.setFrom(from);
            //邮件接收人,设置多个收件人地址
            InternetAddress[] internetAddressTo = InternetAddress.parse(to);
            messageHelper.setTo(internetAddressTo);
            //messageHelper.setTo(to);
            //邮件主题
            message.setSubject(subject);
            //邮件内容，html格式
            messageHelper.setText(content, true);
            //发送
            mailSender.send(message);
            //日志信息
            log.info("邮件已经发送。");
        } catch (Exception e) {
            log.error("发送邮件时发生异常！", e);
        }
    }

    /**
     * 带附件的邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param filePath 附件
     */
    @Override
    public void sendAttachmentsMail(String to, String subject, String content, String filePath) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
            helper.addAttachment(fileName, file);
            mailSender.send(message);
            //日志信息
            log.info("邮件已经发送。");
        } catch (Exception e) {
            log.error("发送邮件时发生异常！", e);
        }
    }


}
