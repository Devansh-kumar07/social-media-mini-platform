package com.connectsphere.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationRequest(
        @NotNull Long userId,
        Long actorId,
        @NotBlank String type,
        @NotBlank String message,
        Long targetId,
        String targetType,
        String deepLinkUrl
) {
}
