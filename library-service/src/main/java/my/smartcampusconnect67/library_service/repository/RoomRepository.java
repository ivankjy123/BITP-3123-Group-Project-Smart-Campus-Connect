package my.smartcampusconnect67.library_service.repository;

import my.smartcampusconnect67.library_service.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Optional<Room> findByRoomId(String roomId);
}
