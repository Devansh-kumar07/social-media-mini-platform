package com.connectsphere.notification.service;

import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import java.util.List;

public interface NotificationService {
    NotificationResponse create(CreateNotificationRequest request);

    List<NotificationResponse> createBulk(BulkNotificationRequest request);

    List<NotificationResponse> listByUser(Long userId);

    long unreadCount(Long userId);

    NotificationResponse markRead(Long notificationId, Long userId);

    void markAllRead(Long userId);

    void delete(Long notificationId, Long userId);
}
