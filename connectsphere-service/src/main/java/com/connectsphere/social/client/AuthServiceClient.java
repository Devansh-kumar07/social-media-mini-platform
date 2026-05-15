package com.connectsphere.social.client;

import com.connectsphere.social.dto.AuthUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthServiceClient {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.auth-url:http://localhost:8081}")
    private String authServiceUrl;

    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AuthUserResponse getUserById(Long userId) {
        try {
            return restTemplate.getForObject(
                    authServiceUrl + "/api/v1/auth/users/{userId}",
                    AuthUserResponse.class,
                    userId
            );
        } catch (Exception e) {
            log.warn("Could not fetch user {} from auth-service. Reason: {}", userId, e.getMessage());
            return null;
        }
    }

    public AuthUserResponse getUserByUsername(String username) {
        try {
            return restTemplate.getForObject(
                    authServiceUrl + "/api/v1/auth/users/username/{username}",
                    AuthUserResponse.class,
                    username
            );
        } catch (Exception e) {
            log.warn("Could not fetch username {} from auth-service. Reason: {}", username, e.getMessage());
            return null;
        }
    }
}
