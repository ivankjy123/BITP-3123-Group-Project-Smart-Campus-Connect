package my.smartcampusconnect67.library_service.repository;

import my.smartcampusconnect67.library_service.model.RoomReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomReservationRepository extends JpaRepository<RoomReservation, Integer> {
    List<RoomReservation> findByStudentMatricNo(String matricNo);
    List<RoomReservation> findByRoomId(String roomId);
    List<RoomReservation> findByRoomIdAndStartTimeAfter(String roomId, LocalDateTime now);

    // Check for overlapping reservations for a room
    @Query("SELECT r FROM RoomReservation r WHERE r.roomId = :roomId " + "AND r.startTime < :end AND r.endTime > :start")
    List<RoomReservation> findOverlapping(String roomId, LocalDateTime start, LocalDateTime end);
}
