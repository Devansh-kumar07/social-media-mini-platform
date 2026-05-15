package com.connectsphere.social.service;

import com.connectsphere.social.client.AuthServiceClient;
import com.connectsphere.social.dto.AuthUserResponse;
import org.springframework.stereotype.Service;

@Service
public class AdminModerationService {
    private final AuthServiceClient authServiceClient;

    public AdminModerationService(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public void ensureAdmin(Long userId) {
        AuthUserResponse user = authServiceClient.getUserById(userId);
        if (user == null || user.role() == null || !"ADMIN".equalsIgnoreCase(String.valueOf(user.role()))) {
            throw new IllegalArgumentException("Only admin can perform this action");
        }
    }
}
