package com.roombooker.service;

import com.roombooker.dto.RoomRequest;
import com.roombooker.dto.RoomResponse;
import com.roombooker.exception.InvalidMeetingTimeException;
import com.roombooker.exception.ResourceNotFoundException;
import com.roombooker.repository.RoomRepository;
import com.roombooker.rooms.Room;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @CacheEvict(value = "rooms", allEntries = true)
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByName(request.name())) {
            throw new InvalidMeetingTimeException("Room with name '" + request.name() + "' already exists");
        }

        Room room = Room.builder()
            .name(request.name())
            .capacity(request.capacity())
            .location(request.location())
            .build();

        Room saved = roomRepository.save(room);
        return toRoomResponse(saved);
    }

    @Cacheable(value = "rooms")
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
            .map(this::toRoomResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(UUID id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
        return toRoomResponse(room);
    }

    @CacheEvict(value = "rooms", allEntries = true)
    @Transactional
    public RoomResponse updateRoom(UUID id, RoomRequest request) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        room.setName(request.name());
        room.setCapacity(request.capacity());
        room.setLocation(request.location());

        Room updated = roomRepository.save(room);
        return toRoomResponse(updated);
    }

    @CacheEvict(value = "rooms", allEntries = true)
    @Transactional
    public void deleteRoom(UUID id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with ID: " + id);
        }
        roomRepository.deleteById(id);
    }

    public RoomResponse toRoomResponse(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getName(),
            room.getCapacity(),
            room.getLocation(),
            room.getCreatedAt()
        );
    }
}
