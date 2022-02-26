package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {
    // 记录日志
    private static Logger logger = LoggerFactory.getLogger(MailClient.class);

    // Spring的email核心组件
    @Autowired
    private JavaMailSender javaMailSender;

    // 注入发件人属性值
    @Value("${spring.mail.username}")
    private String from;

    // 发送邮件发放封装
    public void sendMail(String to, String subject, String content) {
        try {
            // 使用组件创建邮件对象
            MimeMessage message = javaMailSender.createMimeMessage();
            // 使用MimeMessageHelper构建工具构建邮件信息
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            // 发送构建好的邮件
            javaMailSender.send(helper.getMimeMessage()); // 注意从helper类中获取
        } catch (MessagingException e) {
            logger.error("发送邮件失败:" + e.getMessage());
        }
    }
}
