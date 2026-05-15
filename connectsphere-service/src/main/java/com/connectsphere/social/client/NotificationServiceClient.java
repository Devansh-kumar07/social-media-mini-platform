package com.connectsphere.social.client;

import com.connectsphere.social.dto.NotificationEvent;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * This class handles all HTTP calls to the notification-service.
 *
 * Whenever something important happens (someone follows you, likes your post,
 * comments on your post), we call this client to create a notification
 * for the affected user.
 *
 * Again — all wrapped in try-catch so the main operation doesn't fail
 * even if notification-service is temporarily down.
 */
@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${services.notification-url:http://localhost:8085}")
    private String notificationServiceUrl;

    @Value("${connectsphere.rabbitmq.notification-exchange}")
    private String notificationExchange;

    @Value("${connectsphere.rabbitmq.notification-routing-key}")
    private String notificationRoutingKey;

    public NotificationServiceClient(RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send a notification to a user.
     *
     * @param userId   The user who will RECEIVE the notification
     * @param actorId  The user who CAUSED the notification (e.g., who liked/followed)
     * @param type     Type like "FOLLOW", "LIKE", "COMMENT", "REPLY"
     * @param message  Human-readable message like "Ahmed followed you"
     */
    public void sendNotification(Long userId, Long actorId, String type, String message) {
        sendNotification(userId, actorId, type, message, null, null);
    }

    public void sendNotification(
            Long userId,
            Long actorId,
            String type,
            String message,
            Long targetId,
            String targetType
    ) {
        try {
            // Don't notify yourself (e.g., if you like your own post)
            if (actorId != null && userId.equals(actorId)) {
                log.info("Skipping self-notification for user {}", userId);
                return;
            }

            NotificationEvent event = new NotificationEvent(
                    userId,
                    actorId,
                    type,
                    message,
                    targetId,
                    targetType,
                    null
            );

            rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, event);
            log.info("Queued notification event of type '{}' for user {}", type, userId);

        } catch (Exception rabbitException) {
            log.error("Could not publish '{}' notification to RabbitMQ. Falling back to REST. Reason: {}", type, rabbitException.getMessage());

            try {
                String url = notificationServiceUrl + "/api/v1/notifications";

                Map<String, Object> body = new HashMap<>();
                body.put("userId", userId);
                body.put("actorId", actorId);
                body.put("type", type);
                body.put("message", message);
                body.put("targetId", targetId);
                body.put("targetType", targetType);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                restTemplate.postForObject(url, request, Object.class);
                log.info("Notification of type '{}' sent to user {} using REST fallback", type, userId);

            } catch (Exception restException) {
                log.error("Could not send '{}' notification to user {} even with REST fallback. Reason: {}", type, userId, restException.getMessage());
            }
        }
    }
}
