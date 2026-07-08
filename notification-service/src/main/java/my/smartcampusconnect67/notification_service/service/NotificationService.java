package my.smartcampusconnect67.notification_service.service;

import my.smartcampusconnect67.notification_service.dto.NotificationMessage;
import my.smartcampusconnect67.notification_service.model.NotificationLog;
import my.smartcampusconnect67.notification_service.repository.NotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationLogRepository logRepository;

    public void logNotification(NotificationMessage msg) {
        NotificationLog log = new NotificationLog(
                msg.getRecipientEmail(),
                msg.getEventType(),
                msg.getMessageBody(),
                "SENT"
        );
        logRepository.save(log);
    }

    public List<NotificationLog> getAllNotifications() {
        return logRepository.findAll();
    }

    public List<NotificationLog> getNotificationsByEventType(String eventType) {
        return logRepository.findByEventType(eventType);
    }

    public List<NotificationLog> getNotificationsByEmail(String email) {
        return logRepository.findByRecipientEmail(email);
    }
}
