package com.connectsphere.auth.config;

import com.connectsphere.auth.model.AuthProvider;
import com.connectsphere.auth.model.User;
import com.connectsphere.auth.model.UserRole;
import com.connectsphere.auth.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class AdminBootstrapConfig {

    @Bean
    ApplicationRunner adminBootstrapRunner(
            AdminBootstrapProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!properties.isEnabled()) {
                return;
            }

            User adminUser = userRepository.findByEmail(properties.getEmail())
                    .orElseGet(User::new);

            adminUser.setEmail(properties.getEmail());
            adminUser.setUsername(properties.getUsername());
            adminUser.setFullName(properties.getFullName());
            adminUser.setRole(UserRole.ADMIN);
            adminUser.setProvider(AuthProvider.LOCAL);
            adminUser.setActive(true);

            if (properties.isSyncPasswordOnStartup()
                    || adminUser.getPasswordHash() == null
                    || adminUser.getPasswordHash().isBlank()) {
                adminUser.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
            }

            userRepository.save(adminUser);
        };
    }
}
