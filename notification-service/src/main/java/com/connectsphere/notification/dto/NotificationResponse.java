package com.connectsphere.notification.dto;

import com.connectsphere.notification.model.Notification;
import java.time.Instant;

public record NotificationResponse(
        Long notificationId,
        Long userId,
        Long actorId,
        String type,
        String message,
        Long targetId,
        String targetType,
        String deepLinkUrl,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getUserId(),
                notification.getActorId(),
                notification.getType(),
                notification.getMessage(),
                notification.getTargetId(),
                notification.getTargetType(),
                notification.getDeepLinkUrl(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
