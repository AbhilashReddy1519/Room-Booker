package com.roombooker.repository;

import com.roombooker.booking.MeetingAttendee;
import com.roombooker.booking.MeetingAttendeeId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee, MeetingAttendeeId> {
    List<MeetingAttendee> findByMeetingId(UUID meetingId);
    List<MeetingAttendee> findByUserId(UUID userId);
    boolean existsByMeetingIdAndUserId(UUID meetingId, UUID userId);
}
