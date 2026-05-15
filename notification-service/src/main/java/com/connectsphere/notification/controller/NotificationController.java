package com.connectsphere.notification.controller;

import com.connectsphere.notification.dto.BulkNotificationRequest;
import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.dto.NotificationResponse;
import com.connectsphere.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/health")
    @Operation(summary = "Check notification service health")
    public String health() {
        return "notification-service is running";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a notification")
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.create(request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create notifications in bulk")
    public List<NotificationResponse> createBulk(@Valid @RequestBody BulkNotificationRequest request) {
        return notificationService.createBulk(request);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List notifications for a user")
    public List<NotificationResponse> listByUser(@PathVariable("userId") Long userId) {
        return notificationService.listByUser(userId);
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notification count")
    public long unreadCount(@PathVariable("userId") Long userId) {
        return notificationService.unreadCount(userId);
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public NotificationResponse markRead(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam(name = "userId") Long userId
    ) {
        return notificationService.markRead(notificationId, userId);
    }

    @PutMapping("/user/{userId}/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark all notifications as read")
    public void markAllRead(@PathVariable("userId") Long userId) {
        notificationService.markAllRead(userId);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a notification")
    public void delete(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam(name = "userId") Long userId
    ) {
        notificationService.delete(notificationId, userId);
    }
}
