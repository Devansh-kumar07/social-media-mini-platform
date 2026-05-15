package com.connectsphere.auth.controller;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.ChangePasswordRequest;
import com.connectsphere.auth.dto.ForgotPasswordRequest;
import com.connectsphere.auth.dto.LoginRequest;
import com.connectsphere.auth.dto.RefreshTokenRequest;
import com.connectsphere.auth.dto.RegisterRequest;
import com.connectsphere.auth.dto.ResetPasswordRequest;
import com.connectsphere.auth.dto.SimpleMessageResponse;
import com.connectsphere.auth.dto.TokenValidationRequest;
import com.connectsphere.auth.dto.TokenValidationResponse;
import com.connectsphere.auth.dto.UpdateProfileRequest;
import com.connectsphere.auth.dto.UpdateUserRoleRequest;
import com.connectsphere.auth.dto.UserResponse;
import com.connectsphere.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/health")
    @Operation(summary = "Check auth service health")
    public String health() {
        return "auth-service is running";
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh an access token")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    public SimpleMessageResponse logout() {
        return authService.logout();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send a password reset link to the user's email")
    public SimpleMessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.requestPasswordReset(request);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password using emailed token")
    public SimpleMessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate an access token")
    public TokenValidationResponse validateToken(@Valid @RequestBody TokenValidationRequest request) {
        return authService.validateToken(request.token());
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by id")
    public UserResponse getUser(@PathVariable("userId") Long userId) {
        return authService.getUser(userId);
    }

    @GetMapping("/users/username/{username}")
    @Operation(summary = "Get user by username")
    public UserResponse getUserByUsername(@PathVariable("username") String username) {
        return authService.getUserByUsername(username);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users")
    public Page<UserResponse> searchUsers(@RequestParam(name = "q", defaultValue = "") String q, Pageable pageable) {
        return authService.searchUsers(q, pageable);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get logged in user profile", security = @SecurityRequirement(name = "bearer-jwt"))
    public UserResponse profile(Authentication authentication) {
        return authService.getUser(currentUserId(authentication));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update logged in user profile", security = @SecurityRequirement(name = "bearer-jwt"))
    public UserResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return authService.updateProfile(currentUserId(authentication), request);
    }

    @PutMapping("/admin/users/{userId}/role")
    @Operation(summary = "Update a user's role as admin", security = @SecurityRequirement(name = "bearer-jwt"))
    public UserResponse updateUserRole(
            Authentication authentication,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return authService.updateUserRole(currentUserId(authentication), userId, request);
    }

    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload logged in user profile picture", security = @SecurityRequirement(name = "bearer-jwt"))
    public UserResponse uploadProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        return authService.uploadProfilePicture(currentUserId(authentication), file);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change logged in user password", security = @SecurityRequirement(name = "bearer-jwt"))
    public void changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(currentUserId(authentication), request);
    }

    @DeleteMapping("/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate logged in user account", security = @SecurityRequirement(name = "bearer-jwt"))
    public void deactivateAccount(Authentication authentication) {
        authService.deactivateAccount(currentUserId(authentication));
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
