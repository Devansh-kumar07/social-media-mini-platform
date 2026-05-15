package com.connectsphere.auth.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String fullName, String resetLink);
}
