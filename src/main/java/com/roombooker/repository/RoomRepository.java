package com.roombooker.repository;

import com.roombooker.rooms.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    boolean existsByName(String name);
}
