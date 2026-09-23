package com.roombooker.booking;

import com.roombooker.users.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "meeting_attendees",
    indexes = {
        @Index(name = "idx_meeting_attendees_user", columnList = "user_id")
    }
)
public class MeetingAttendee {

    @EmbeddedId
    private MeetingAttendeeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("meetingId")
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_status", nullable = false, length = 30)
    private AttendeeResponseStatus responseStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public MeetingAttendee() {}

    public MeetingAttendee(MeetingAttendeeId id, Meeting meeting, User user, AttendeeResponseStatus responseStatus, Instant createdAt) {
        this.id = id;
        this.meeting = meeting;
        this.user = user;
        this.responseStatus = responseStatus;
        this.createdAt = createdAt;
    }

    public static MeetingAttendeeBuilder builder() {
        return new MeetingAttendeeBuilder();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (responseStatus == null) {
            responseStatus = AttendeeResponseStatus.PENDING;
        }
    }

    public MeetingAttendeeId getId() { return id; }
    public void setId(MeetingAttendeeId id) { this.id = id; }

    public Meeting getMeeting() { return meeting; }
    public void setMeeting(Meeting meeting) { this.meeting = meeting; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public AttendeeResponseStatus getResponseStatus() { return responseStatus; }
    public void setResponseStatus(AttendeeResponseStatus responseStatus) { this.responseStatus = responseStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class MeetingAttendeeBuilder {
        private MeetingAttendeeId id;
        private Meeting meeting;
        private User user;
        private AttendeeResponseStatus responseStatus;
        private Instant createdAt;

        public MeetingAttendeeBuilder id(MeetingAttendeeId id) { this.id = id; return this; }
        public MeetingAttendeeBuilder meeting(Meeting meeting) { this.meeting = meeting; return this; }
        public MeetingAttendeeBuilder user(User user) { this.user = user; return this; }
        public MeetingAttendeeBuilder responseStatus(AttendeeResponseStatus responseStatus) { this.responseStatus = responseStatus; return this; }
        public MeetingAttendeeBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public MeetingAttendee build() {
            return new MeetingAttendee(id, meeting, user, responseStatus, createdAt);
        }
    }
}
