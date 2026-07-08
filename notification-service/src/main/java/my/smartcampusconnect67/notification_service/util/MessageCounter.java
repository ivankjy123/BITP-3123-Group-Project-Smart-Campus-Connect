package my.smartcampusconnect67.notification_service.util;

import my.smartcampusconnect67.notification_service.NotificationServiceApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/stats")
public class MessageCounter {

    @Autowired
    private NotificationServiceApplication notificationApplication;

    @GetMapping("/counts")
    public ConcurrentHashMap<String, Long> getMessageCounts() {
        return notificationApplication.getMessageCountPerType();
    }
}
