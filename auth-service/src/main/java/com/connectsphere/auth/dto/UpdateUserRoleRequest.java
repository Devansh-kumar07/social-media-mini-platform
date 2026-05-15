package com.connectsphere.auth.dto;

import com.connectsphere.auth.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull UserRole role
) {
}
