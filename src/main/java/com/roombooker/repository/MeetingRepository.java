package com.roombooker.repository;

import com.roombooker.booking.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    List<Meeting> findByOrganizerId(UUID organizerId);
}
