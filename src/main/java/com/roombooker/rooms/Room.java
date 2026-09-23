package com.roombooker.rooms;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Room() {}

    public Room(UUID id, String name, Integer capacity, String location, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.createdAt = createdAt;
    }

    public static RoomBuilder builder() {
        return new RoomBuilder();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class RoomBuilder {
        private UUID id;
        private String name;
        private Integer capacity;
        private String location;
        private Instant createdAt;

        public RoomBuilder id(UUID id) { this.id = id; return this; }
        public RoomBuilder name(String name) { this.name = name; return this; }
        public RoomBuilder capacity(Integer capacity) { this.capacity = capacity; return this; }
        public RoomBuilder location(String location) { this.location = location; return this; }
        public RoomBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Room build() {
            return new Room(id, name, capacity, location, createdAt);
        }
    }
}
