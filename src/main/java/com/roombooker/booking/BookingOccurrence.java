package com.roombooker.booking;

import com.roombooker.rooms.Room;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "booking_occurrences",
    indexes = {
        @Index(name = "idx_booking_occurrences_room_start", columnList = "room_id,start_time"),
        @Index(name = "idx_booking_occurrences_room_end", columnList = "room_id,end_time"),
        @Index(name = "idx_booking_occurrences_series", columnList = "series_id"),
        @Index(name = "idx_booking_occurrences_meeting", columnList = "meeting_id"),
        @Index(name = "idx_booking_occurrences_room_date", columnList = "room_id,occurrence_date")
    }
)
public class BookingOccurrence {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private BookingSeries series;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BookingOccurrence() {}

    public BookingOccurrence(UUID id, Meeting meeting, BookingSeries series, Room room, Instant startTime, Instant endTime, LocalDate occurrenceDate, BookingStatus status, Instant createdAt) {
        this.id = id;
        this.meeting = meeting;
        this.series = series;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.occurrenceDate = occurrenceDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static BookingOccurrenceBuilder builder() {
        return new BookingOccurrenceBuilder();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = BookingStatus.CONFIRMED;
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Meeting getMeeting() { return meeting; }
    public void setMeeting(Meeting meeting) { this.meeting = meeting; }

    public BookingSeries getSeries() { return series; }
    public void setSeries(BookingSeries series) { this.series = series; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public LocalDate getOccurrenceDate() { return occurrenceDate; }
    public void setOccurrenceDate(LocalDate occurrenceDate) { this.occurrenceDate = occurrenceDate; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class BookingOccurrenceBuilder {
        private UUID id;
        private Meeting meeting;
        private BookingSeries series;
        private Room room;
        private Instant startTime;
        private Instant endTime;
        private LocalDate occurrenceDate;
        private BookingStatus status;
        private Instant createdAt;

        public BookingOccurrenceBuilder id(UUID id) { this.id = id; return this; }
        public BookingOccurrenceBuilder meeting(Meeting meeting) { this.meeting = meeting; return this; }
        public BookingOccurrenceBuilder series(BookingSeries series) { this.series = series; return this; }
        public BookingOccurrenceBuilder room(Room room) { this.room = room; return this; }
        public BookingOccurrenceBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
        public BookingOccurrenceBuilder endTime(Instant endTime) { this.endTime = endTime; return this; }
        public BookingOccurrenceBuilder occurrenceDate(LocalDate occurrenceDate) { this.occurrenceDate = occurrenceDate; return this; }
        public BookingOccurrenceBuilder status(BookingStatus status) { this.status = status; return this; }
        public BookingOccurrenceBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public BookingOccurrence build() {
            return new BookingOccurrence(id, meeting, series, room, startTime, endTime, occurrenceDate, status, createdAt);
        }
    }
}
