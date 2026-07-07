package my.smartcampusconnect67.student_service.client;

import my.smartcampusconnect67.student_service.dto.NotificationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Component
public class NotificationTcpClient {

    @Value("${notification.tcp.host}")
    private String host;

    @Value("${notification.tcp.port}")
    private int port;

    private final ObjectMapper mapper = new ObjectMapper();

    public void send(NotificationMessage message) {
        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            byte[] json = mapper.writeValueAsBytes(message);
            out.writeInt(json.length);   // length prefix (4 bytes, big-endian)
            out.write(json);
            out.flush();

        } catch (IOException e) {
            // Handle gracefully – maybe log and not rethrow, since notification is not critical
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
