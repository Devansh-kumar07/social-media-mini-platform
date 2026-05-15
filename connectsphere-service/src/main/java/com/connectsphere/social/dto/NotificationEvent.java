package com.connectsphere.social.dto;

public record NotificationEvent(
        Long userId,
        Long actorId,
        String type,
        String message,
        Long targetId,
        String targetType,
        String deepLinkUrl
) {
}
