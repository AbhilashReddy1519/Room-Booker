package com.roombooker.repository;

import com.roombooker.booking.BookingSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingSeriesRepository extends JpaRepository<BookingSeries, UUID> {
    Optional<BookingSeries> findByMeetingId(UUID meetingId);
}
