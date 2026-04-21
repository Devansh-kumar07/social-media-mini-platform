package com.connectsphere.auth.security;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.UserResponse;
import com.connectsphere.auth.model.AuthProvider;
import com.connectsphere.auth.model.User;
import com.connectsphere.auth.repository.UserRepository;
import com.connectsphere.auth.service.JwtService;
import com.connectsphere.auth.service.JwtToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String fullName = Optional.ofNullable(oauthUser.<String>getAttribute("name")).orElse(email);
        String picture = oauthUser.getAttribute("picture");

        User user = userRepository.findByEmail(email).orElseGet(User::new);
        if (user.getUserId() == null) {
            user.setEmail(email);
            user.setUsername(uniqueUsername(email));
            user.setFullName(fullName);
            user.setProvider(AuthProvider.GOOGLE);
        }
        user.setFullName(fullName);
        user.setProfilePicUrl(picture);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        JwtToken token = jwtService.generateToken(savedUser);
        AuthResponse authResponse = new AuthResponse(token.token(), "Bearer", token.expiresAt(), UserResponse.from(savedUser));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), authResponse);
    }

    private String uniqueUsername(String email) {
        String base = email.substring(0, email.indexOf("@"))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "");
        if (base.length() < 3) {
            base = "user" + base;
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }
}

