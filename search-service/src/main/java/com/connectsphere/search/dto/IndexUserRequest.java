package com.connectsphere.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IndexUserRequest(
        @NotNull Long userId,
        @NotBlank String username,
        String fullName
) {
}
