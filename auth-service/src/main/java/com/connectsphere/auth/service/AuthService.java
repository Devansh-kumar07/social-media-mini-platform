package com.connectsphere.auth.service;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.ChangePasswordRequest;
import com.connectsphere.auth.dto.ForgotPasswordRequest;
import com.connectsphere.auth.dto.LoginRequest;
import com.connectsphere.auth.dto.RefreshTokenRequest;
import com.connectsphere.auth.dto.RegisterRequest;
import com.connectsphere.auth.dto.ResetPasswordRequest;
import com.connectsphere.auth.dto.SimpleMessageResponse;
import com.connectsphere.auth.dto.TokenValidationResponse;
import com.connectsphere.auth.dto.UpdateProfileRequest;
import com.connectsphere.auth.dto.UpdateUserRoleRequest;
import com.connectsphere.auth.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    SimpleMessageResponse logout();

    SimpleMessageResponse requestPasswordReset(ForgotPasswordRequest request);

    SimpleMessageResponse resetPassword(ResetPasswordRequest request);

    TokenValidationResponse validateToken(String token);

    Page<UserResponse> searchUsers(String query, Pageable pageable);

    UserResponse getUser(Long userId);

    UserResponse getUserByUsername(String username);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    UserResponse updateUserRole(Long adminUserId, Long targetUserId, UpdateUserRoleRequest request);

    UserResponse uploadProfilePicture(Long userId, MultipartFile file);

    void changePassword(Long userId, ChangePasswordRequest request);

    void deactivateAccount(Long userId);
}
