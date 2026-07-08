package my.smartcampusconnect67.library_service.dto;

import java.io.Serial;
import java.io.Serializable;

public class NotificationMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Passing Variable
    private String eventType;
    private String recipientEmail;
    private String messageBody;
    private long timestamp;

    public NotificationMessage() {}
    public NotificationMessage(String eventType, String recipientEmail, String messageBody, long timestamp) {
        this.eventType = eventType;
        this.recipientEmail = recipientEmail;
        this.messageBody = messageBody;
        this.timestamp = timestamp;
    }

    // Setter Getter
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // toString() Method
    @Override
    public String toString() {
        return "NotificationMessage{" +
                "eventType='" + eventType + '\'' +
                ", recipientEmail='" + recipientEmail + '\'' +
                ", messageBody='" + messageBody + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
