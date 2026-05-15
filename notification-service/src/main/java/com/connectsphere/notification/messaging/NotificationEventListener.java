package com.connectsphere.notification.messaging;

import com.connectsphere.notification.dto.CreateNotificationRequest;
import com.connectsphere.notification.dto.NotificationEvent;
import com.connectsphere.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${connectsphere.rabbitmq.notification-queue}")
    public void consume(NotificationEvent event) {
        log.info("Received notification event for user {} of type {}", event.userId(), event.type());
        notificationService.create(new CreateNotificationRequest(
                event.userId(),
                event.actorId(),
                event.type(),
                event.message(),
                event.targetId(),
                event.targetType(),
                event.deepLinkUrl()
        ));
    }
}
