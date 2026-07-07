package my.smartcampusconnect67.notification_service.controller;

import my.smartcampusconnect67.notification_service.model.NotificationLog;
import my.smartcampusconnect67.notification_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // GET /api/notifications
    @GetMapping
    public ResponseEntity<List<NotificationLog>> getAllNotifications() {
        List<NotificationLog> logs = notificationService.getAllNotifications();
        return ResponseEntity.ok(logs);
    }

    // GET /api/notifications/type/{eventType}
    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<NotificationLog>> getByEventType(@PathVariable String eventType) {
        List<NotificationLog> logs = notificationService.getNotificationsByEventType(eventType);
        return ResponseEntity.ok(logs);
    }

    // GET /api/notifications/email/{email}
    @GetMapping("/email/{email}")
    public ResponseEntity<List<NotificationLog>> getByEmail(@PathVariable String email) {
        List<NotificationLog> logs = notificationService.getNotificationsByEmail(email);
        return ResponseEntity.ok(logs);
    }
}
