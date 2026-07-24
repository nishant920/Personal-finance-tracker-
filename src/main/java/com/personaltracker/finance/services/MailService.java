package com.personaltracker.finance.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String name, String token) {
        String verificationLink = appBaseUrl + "/api/auth/verify?token=" + token;
        log.info("Sending verification email to {} with link {}", toEmail, verificationLink);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Account Verification - Personal Finance Tracker");
            message.setText("Hi " + name + ",\n\nPlease verify your account by clicking the following link:\n" + verificationLink);
            mailSender.send(message);
            log.info("Verification email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }
}
