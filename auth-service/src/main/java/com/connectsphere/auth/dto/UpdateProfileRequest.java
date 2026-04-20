package com.connectsphere.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 3, max = 60) String username,
        @Email String email,
        @Size(max = 120) String fullName,
        @Size(max = 500) String bio,
        @Size(max = 255) String profilePicUrl
) {
}

