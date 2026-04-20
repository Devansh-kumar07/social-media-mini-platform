package com.connectsphere.auth.service;

import com.connectsphere.auth.dto.RegisterRequest;
import com.connectsphere.auth.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    Page<UserResponse> searchUsers(String query, Pageable pageable);

    UserResponse getUser(Long userId);
}

