package com.connectsphere.notification.repository;

import com.connectsphere.notification.model.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalseAndDeletedFalse(Long userId);

    List<Notification> findByUserIdAndReadFalseAndDeletedFalse(Long userId);
}
