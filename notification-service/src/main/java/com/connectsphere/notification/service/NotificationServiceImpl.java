package com.connectsphere.notification.service;

import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import com.connectsphere.notification.model.Notification;
import com.connectsphere.notification.repository.NotificationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification();
        applyRequest(notification, request.userId(), request.actorId(), request.type(), request.message(), request.targetId(), request.targetType(), request.deepLinkUrl());

        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public List<NotificationResponse> createBulk(BulkNotificationRequest request) {
        return request.userIds().stream()
                .map(userId -> {
                    Notification notification = new Notification();
                    applyRequest(notification, userId, request.actorId(), request.type(), request.message(), request.targetId(), request.targetType(), request.deepLinkUrl());
                    return NotificationResponse.from(notificationRepository.save(notification));
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listByUser(Long userId) {
        return notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Long notificationId, Long userId) {
        Notification notification = findOwnedNotification(notificationId, userId);
        notification.setRead(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        for (Notification notification : notificationRepository.findByUserIdAndReadFalseAndDeletedFalse(userId)) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void delete(Long notificationId, Long userId) {
        Notification notification = findOwnedNotification(notificationId, userId);
        notification.setDeleted(true);
        notificationRepository.save(notification);
    }

    private Notification findOwnedNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (notification.isDeleted() || !notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Notification not found");
        }
        return notification;
    }

    private void applyRequest(
            Notification notification,
            Long userId,
            Long actorId,
            String type,
            String message,
            Long targetId,
            String targetType,
            String deepLinkUrl
    ) {
        notification.setUserId(userId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setDeepLinkUrl(deepLinkUrl);
        notification.setRead(false);
        notification.setDeleted(false);
    }
}
