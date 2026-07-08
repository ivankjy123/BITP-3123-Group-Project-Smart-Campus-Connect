package my.smartcampusconnect67.library_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_reservations")
public class RoomReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_id", nullable = false, length = 20)
    private String roomId;

    @Column(name = "student_matric_no", nullable = false, length = 20)
    private String studentMatricNo;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    public RoomReservation() {}

    public RoomReservation(String roomId, String studentMatricNo, LocalDateTime startTime, LocalDateTime endTime) {
        this.roomId = roomId;
        this.studentMatricNo = studentMatricNo;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getStudentMatricNo() { return studentMatricNo; }
    public void setStudentMatricNo(String studentMatricNo) { this.studentMatricNo = studentMatricNo; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
