package my.smartcampusconnect67.notification_service.repository;

import my.smartcampusconnect67.notification_service.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Integer> {
    List<NotificationLog> findByEventType(String eventType);
    List<NotificationLog> findByRecipientEmail(String recipientEmail);
}
