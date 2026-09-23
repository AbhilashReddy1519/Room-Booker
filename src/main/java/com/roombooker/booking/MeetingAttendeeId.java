package com.roombooker.booking;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class MeetingAttendeeId implements Serializable {
    private UUID meetingId;
    private UUID userId;

    public MeetingAttendeeId() {}

    public MeetingAttendeeId(UUID meetingId, UUID userId) {
        this.meetingId = meetingId;
        this.userId = userId;
    }

    public UUID getMeetingId() { return meetingId; }
    public void setMeetingId(UUID meetingId) { this.meetingId = meetingId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingAttendeeId that = (MeetingAttendeeId) o;
        return Objects.equals(meetingId, that.meetingId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meetingId, userId);
    }
}
