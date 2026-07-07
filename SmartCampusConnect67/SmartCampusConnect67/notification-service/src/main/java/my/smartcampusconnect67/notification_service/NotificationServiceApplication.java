package my.smartcampusconnect67.notification_service;

import jakarta.annotation.PostConstruct;
import my.smartcampusconnect67.notification_service.controller.ClientHandler;
import my.smartcampusconnect67.notification_service.service.NotificationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class NotificationServiceApplication implements Runnable {
	private final ExecutorService pool = Executors.newFixedThreadPool(10);

	// Shared mutable state: counts messages per event type (protected by ConcurrentHashMap)
	private final ConcurrentHashMap<String, Long> messageCountPerType = new ConcurrentHashMap<>();

	// Autowired service for persisting notifications
	@Autowired
	private NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Override
	public void run() {
		int port = Integer.parseInt(System.getProperty("tcp.server.port", "9090"));
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Notification TCP server listening on port " + port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				pool.execute(new ClientHandler(clientSocket, this));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@PostConstruct
	public void startTcpServer() {
		new Thread(this, "TcpServerThread").start();
	}

	// Increment the counter atomically
	public void incrementMessageCount(String eventType) {
		messageCountPerType.merge(eventType, 1L, Long::sum);
	}

	// Getter for the whole map (used by REST endpoint)
	public ConcurrentHashMap<String, Long> getMessageCountPerType() {
		return messageCountPerType;
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}
}
