package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    public void sendWelcomeEmail(String to, String username) {
        // 模拟发送邮件
        logger.info("模拟发送欢迎邮件至: {} 用户: {}", to, username);
    }
    
    public void sendPasswordResetEmail(String to, String resetToken) {
        // 模拟发送密码重置邮件
        logger.info("模拟发送密码重置邮件至: {} Token: {}", to, resetToken);
    }
}