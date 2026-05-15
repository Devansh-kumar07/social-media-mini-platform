package com.connectsphere.auth.service;

import com.connectsphere.auth.client.MediaServiceClient;
import com.connectsphere.auth.client.SearchServiceClient;
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
import com.connectsphere.auth.model.UserRole;
import com.connectsphere.auth.model.User;
import com.connectsphere.auth.repository.UserRepository;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MediaServiceClient mediaServiceClient;
    private final SearchServiceClient searchServiceClient;
    private final EmailService emailService;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final String frontendResetPasswordUrl;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           MediaServiceClient mediaServiceClient,
                           SearchServiceClient searchServiceClient,
                           EmailService emailService,
                           PasswordResetTokenStore passwordResetTokenStore,
                           @Value("${connectsphere.auth.reset-password.frontend-url:http://localhost:4200/reset-password}") String frontendResetPasswordUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mediaServiceClient = mediaServiceClient;
        this.searchServiceClient = searchServiceClient;
        this.emailService = emailService;
        this.passwordResetTokenStore = passwordResetTokenStore;
        this.frontendResetPasswordUrl = frontendResetPasswordUrl;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());

        User savedUser = userRepository.save(user);
        searchServiceClient.indexUser(savedUser.getUserId(), savedUser.getUsername(), savedUser.getFullName());

        return UserResponse.from(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!user.isActive() || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        JwtToken jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken.token(), "Bearer", jwtToken.expiresAt(), UserResponse.from(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        JwtClaims claims = jwtService.validateToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Token is invalid or expired"));

        User user = findActiveUser(claims.userId());
        JwtToken jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken.token(), "Bearer", jwtToken.expiresAt(), UserResponse.from(user));
    }

    @Override
    public SimpleMessageResponse logout() {
        return new SimpleMessageResponse("Logged out successfully");
    }

    @Override
    @Transactional
    public SimpleMessageResponse requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        String token = UUID.randomUUID().toString();
        passwordResetTokenStore.storeToken(token, user.getUserId(), Duration.ofMinutes(30));

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName(),
                frontendResetPasswordUrl + "?token=" + token
        );
        return new SimpleMessageResponse("Password reset link sent to your email");
    }

    @Override
    @Transactional
    public SimpleMessageResponse resetPassword(ResetPasswordRequest request) {
        Long userId = passwordResetTokenStore.getUserIdByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Reset token is invalid or expired"));
        User user = findActiveUser(userId);

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        passwordResetTokenStore.deleteToken(request.token());
        userRepository.save(user);
        return new SimpleMessageResponse("Password reset successful");
    }

    @Override
    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(String token) {
        return jwtService.validateToken(token)
                .map(claims -> new TokenValidationResponse(
                        true,
                        claims.userId(),
                        claims.email(),
                        claims.username(),
                        claims.role()
                ))
                .orElse(new TokenValidationResponse(false, null, null, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        return userRepository
                .findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query, pageable)
                .map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findActiveUser(userId);

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(request.username());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("Email is already registered");
            }
            user.setEmail(request.email());
        }

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.profilePicUrl() != null) {
            user.setProfilePicUrl(request.profilePicUrl());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long adminUserId, Long targetUserId, UpdateUserRoleRequest request) {
        User adminUser = findActiveUser(adminUserId);
        if (adminUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admins can change user roles");
        }

        User targetUser = findActiveUser(targetUserId);
        if (adminUserId.equals(targetUserId) && request.role() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Admins cannot remove their own admin access");
        }

        targetUser.setRole(request.role());
        return UserResponse.from(userRepository.save(targetUser));
    }

    @Override
    @Transactional
    public UserResponse uploadProfilePicture(Long userId, MultipartFile file) {
        User user = findActiveUser(userId);
        user.setProfilePicUrl(mediaServiceClient.uploadProfilePicture(userId, file));
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findActiveUser(userId);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = findActiveUser(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }
        return user;
    }

}
