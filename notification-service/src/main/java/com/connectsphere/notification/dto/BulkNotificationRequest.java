package com.connectsphere.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkNotificationRequest(
        @NotEmpty List<Long> userIds,
        Long actorId,
        @NotBlank String type,
        @NotBlank String message,
        Long targetId,
        String targetType,
        String deepLinkUrl
) {
}
