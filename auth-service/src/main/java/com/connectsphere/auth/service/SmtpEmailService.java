package com.connectsphere.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public SmtpEmailService(
            JavaMailSender mailSender,
            @Value("${connectsphere.mail.from:${spring.mail.username:}}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail);
        }
        message.setTo(toEmail);
        message.setSubject("ConnectSphere password reset");
        message.setText("""
                Hi %s,

                We received a request to reset your ConnectSphere password.

                Reset your password using this link:
                %s

                This link will expire in 30 minutes.

                If you did not request this, you can ignore this email.
                """.formatted(fullName, resetLink));
        mailSender.send(message);
    }
}
