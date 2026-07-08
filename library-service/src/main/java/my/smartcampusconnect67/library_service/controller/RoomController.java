package my.smartcampusconnect67.library_service.controller;

import my.smartcampusconnect67.library_service.model.Room;
import my.smartcampusconnect67.library_service.model.RoomReservation;
import my.smartcampusconnect67.library_service.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        try {
            Room room = roomService.getRoomByRoomId(roomId);
            return ResponseEntity.ok(room);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }

    @PostMapping
    public ResponseEntity<?> addRoom(@RequestBody Room room) {
        try {
            Room saved = roomService.addRoom(room);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable String roomId, @RequestBody Room room) {
        try {
            Room updated = roomService.updateRoom(roomId, room);
            return ResponseEntity.ok(updated);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.accepted().body("Room deleted successfully.");

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }

    @GetMapping("/reservations")
    public List<RoomReservation> getAllReservations() {
        return roomService.getAllReservations();
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<?> getReservationById(@PathVariable Integer reservationId) {
        try {
            RoomReservation reservation = roomService.getReservationById(reservationId);
            return ResponseEntity.ok(reservation);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }

    @GetMapping("/reservations/room/{roomId}")
    public ResponseEntity<?> getReservationsByRoomId(@PathVariable String roomId) {
        try {
            List<RoomReservation> reservations = roomService.getReservationsByRoomId(roomId);
            return ResponseEntity.ok(reservations);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }

    @GetMapping("/reservations/student/{matricNo}")
    public ResponseEntity<?> getReservationsByStudent(@PathVariable String matricNo) {
        try {
            List<RoomReservation> reservations = roomService.getReservationsByStudent(matricNo);;
            return ResponseEntity.ok(reservations);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> reserveRoom(@RequestBody RoomReservation reservation) {
        try {
            RoomReservation res = roomService.reserveRoom(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<?> deleteReservation(@PathVariable Integer reservationId) {
        try {
            roomService.deleteReservation(reservationId);
            return ResponseEntity.accepted().body("Reservation deleted successfully.");

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
        }
    }
}