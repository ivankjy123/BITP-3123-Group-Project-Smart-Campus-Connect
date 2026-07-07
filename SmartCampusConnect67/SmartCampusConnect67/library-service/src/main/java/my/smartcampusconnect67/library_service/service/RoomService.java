package my.smartcampusconnect67.library_service.service;

import my.smartcampusconnect67.library_service.client.NotificationTcpClient;
import my.smartcampusconnect67.library_service.dto.NotificationMessage;
import my.smartcampusconnect67.library_service.model.Room;
import my.smartcampusconnect67.library_service.model.RoomReservation;
import my.smartcampusconnect67.library_service.repository.RoomRepository;
import my.smartcampusconnect67.library_service.repository.RoomReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RoomService {
    private static final String STUDENT_PROFILE_URL = "http://localhost:8081/api/students";
    private final HttpClient client = HttpClient.newHttpClient();

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomReservationRepository reservationRepository;

    @Autowired
    private NotificationTcpClient notificationClient;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomByRoomId(String roomId) {
        return roomRepository.findByRoomId(roomId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Room not found with ID: " + roomId
        ));
    }

    public Room addRoom(Room room) {
        // Disallow adding room with the same room id
        if (roomRepository.findByRoomId(room.getRoomId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room with ID " + room.getRoomId() + " already exists."
            );
        }

        return roomRepository.save(room);
    }

    public Room updateRoom(String roomId, Room updatedRoom) {
        Room existing = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Room not found with ID: " + roomId
                )
        );

        // If the client tries to change the room_id to a different value,
        // check that the new room_id doesn't already exist
        if (!existing.getRoomId().equals(updatedRoom.getRoomId())) {
            if (roomRepository.findByRoomId(updatedRoom.getRoomId()).isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Room ID " + updatedRoom.getRoomId() + " already exists"
                );
            }
            existing.setRoomId(updatedRoom.getRoomId());
        }

        existing.setRoomType(updatedRoom.getRoomType());
        return roomRepository.save(existing);
    }

    public void deleteRoom(String roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found with ID: " + roomId
                )
        );

        // Delete all future reservations for this room (start time > now)
        List<RoomReservation> futureReservations = reservationRepository.findByRoomIdAndStartTimeAfter(roomId, LocalDateTime.now());
        reservationRepository.deleteAll(futureReservations);

        // Now safe to delete the room
        roomRepository.delete(room);
    }

    public List<RoomReservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public RoomReservation getReservationById(Integer reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reservation not found with ID: " + reservationId
                )
        );
    }

    public List<RoomReservation> getReservationsByStudent(String matricNo) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STUDENT_PROFILE_URL + "/" + matricNo))
                .GET()
                .build()
        ;
        String studentEmail = null;

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student with matric No " + matricNo + " not found"
                );
            }

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Unable to contact Student Service."
                );
            }

        } catch (IOException | InterruptedException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Student Service is unavailable."
            );
        }

        return reservationRepository.findByStudentMatricNo(matricNo);
    }

    public List<RoomReservation> getReservationsByRoomId(String roomId) {
        if (roomRepository.findByRoomId(roomId).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Room not found with ID: " + roomId
            );
        }

        return reservationRepository.findByRoomId(roomId);
    }

    public RoomReservation reserveRoom(RoomReservation reservation) {
        String studentMatricNo = reservation.getStudentMatricNo();
        String roomId = reservation.getRoomId();
        LocalDateTime start = reservation.getStartTime();
        LocalDateTime end = reservation.getEndTime();

        // Validate time range
        if (start.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time cannot be in the past. Start: " + start
            );
        }
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End time must be after start time. Start: " + start + ", End: " + end
            );
        }

        // verify if student exist and obtain email
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STUDENT_PROFILE_URL + "/" + studentMatricNo))
                .GET()
                .build()
        ;
        String studentEmail = null;

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student with matric No " + studentMatricNo + " not found"
                );
            }
            if (response.statusCode() != 200) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Unable to contact Student Service."
                );
            }

            // get status and email
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            // Check student status
            JsonNode statusNode = root.get("status");
            String studentStatus = (statusNode != null) ? statusNode.asString() : null;
            if (!"ACTIVE".equalsIgnoreCase(studentStatus)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Student with matric No " + studentMatricNo + " is not active (status: " + studentStatus + "). Cannot reserve room."
                );
            }

            // Extract email
            JsonNode emailNode = root.get("email");
            studentEmail = (emailNode != null) ? emailNode.asString() : null;

        } catch (IOException | InterruptedException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Student Service is unavailable."
            );
        }

        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found with ID: " + roomId
                )
        );

        // Business logic: check room status
        List<RoomReservation> overlaps = reservationRepository.findOverlapping(roomId, start, end);
        if (!overlaps.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room already reserved for the requested time (" + start + " - " + end + ")."
            );
        }

        RoomReservation newReservation = new RoomReservation(roomId, studentMatricNo, start, end);
        newReservation = reservationRepository.save(newReservation);

        // Send notification asynchronously
        if (studentEmail != null) {
            try {
                NotificationMessage msg = new NotificationMessage(
                        "ROOM_RESERVATION",
                        studentEmail,
                        "Room " + roomId + " reserved from " + start + " to " + end,
                        System.currentTimeMillis()
                );
                notificationClient.send(msg);
            } catch (Exception e) {
                System.err.println("Failed to send room reservation notification: " + e.getMessage());
            }
        }

        return newReservation;
    }

    public void deleteReservation(Integer reservationId) {
        RoomReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Reservation not found with ID: " + reservationId
                )
        );

        String studentMatricNo = reservation.getStudentMatricNo();
        String roomId = reservation.getRoomId();
        String studentEmail = null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STUDENT_PROFILE_URL + "/" + studentMatricNo))
                .GET()
                .build()
        ;

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode emailNode = root.get("email");
                studentEmail = (emailNode != null) ? emailNode.asString() : null;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to fetch email for cancellation notification: " + e.getMessage());
        }

        reservationRepository.delete(reservation);

        // Send notification asynchronously
        if (studentEmail != null) {
            try {
                NotificationMessage msg = new NotificationMessage(
                        "ROOM_RESERVATION_CANCELLED",
                        studentEmail,
                        "Your reservation for room " + roomId + " has been cancelled.",
                        System.currentTimeMillis()
                );
                notificationClient.send(msg);

            } catch (Exception e) {
                System.err.println("Failed to send cancellation notification: " + e.getMessage());
            }
        }

    }
}