package com.roombooker.repository;

import com.roombooker.booking.BookingSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BookingSeriesRepository extends JpaRepository<BookingSeries, UUID> {
    Optional<BookingSeries> findByMeetingId(UUID meetingId);
}
