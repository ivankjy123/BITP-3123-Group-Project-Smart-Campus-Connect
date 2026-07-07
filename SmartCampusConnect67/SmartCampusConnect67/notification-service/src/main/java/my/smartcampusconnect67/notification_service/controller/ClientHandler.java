package my.smartcampusconnect67.notification_service.controller;

import my.smartcampusconnect67.notification_service.dto.NotificationMessage;
import my.smartcampusconnect67.notification_service.NotificationServiceApplication;
import tools.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final NotificationServiceApplication app;
    private final ObjectMapper mapper = new ObjectMapper();

    public ClientHandler(Socket socket, NotificationServiceApplication app) {
        this.socket = socket;
        this.app = app;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            // Read length (4-byte big-endian integer)
            int length = in.readInt();
            if (length <= 0 || length > 65536) {
                throw new IOException("Invalid message length: " + length);
            }
            byte[] data = new byte[length];
            in.readFully(data);

            // Deserialize
            NotificationMessage msg = mapper.readValue(data, NotificationMessage.class);

            // Update shared counter (thread‑safe)
            app.incrementMessageCount(msg.getEventType());

            // Persist notification
            app.getNotificationService().logNotification(msg);

            System.out.println("Processed: " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
