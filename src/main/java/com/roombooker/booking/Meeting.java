package com.roombooker.booking;

import com.roombooker.users.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "meetings",
    indexes = {
        @Index(name = "idx_meetings_organizer", columnList = "organizer_id")
    }
)
public class Meeting {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Meeting() {}

    public Meeting(UUID id, String title, User organizer, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.organizer = organizer;
        this.createdAt = createdAt;
    }

    public static MeetingBuilder builder() {
        return new MeetingBuilder();
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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class MeetingBuilder {
        private UUID id;
        private String title;
        private User organizer;
        private Instant createdAt;

        public MeetingBuilder id(UUID id) { this.id = id; return this; }
        public MeetingBuilder title(String title) { this.title = title; return this; }
        public MeetingBuilder organizer(User organizer) { this.organizer = organizer; return this; }
        public MeetingBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Meeting build() {
            return new Meeting(id, title, organizer, createdAt);
        }
    }
}
