package my.smartcampusconnect67.library_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_id", nullable = false, length = 20)
    private String roomId;          // e.g., "DR001"

    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType;        // Discussion Room, Computer Room, Meeting Room

    public Room() {}
    public Room(String roomId, String roomType) {
        this.roomId = roomId;
        this.roomType = roomType;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public String getRoomType() {
        return roomType;
    }
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
}