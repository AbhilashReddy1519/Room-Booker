package com.roombooker.repository;

import com.roombooker.booking.BookingOccurrence;
import com.roombooker.booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingOccurrenceRepository extends JpaRepository<BookingOccurrence, UUID> {

    @Query("""
        SELECT b FROM BookingOccurrence b
        WHERE b.room.id = :roomId
          AND b.status = :status
          AND b.startTime < :requestedEnd
          AND b.endTime > :requestedStart
        ORDER BY b.startTime
        """)
    List<BookingOccurrence> findConflicts(
        @Param("roomId") UUID roomId,
        @Param("requestedStart") Instant requestedStart,
        @Param("requestedEnd") Instant requestedEnd,
        @Param("status") BookingStatus status
    );

    List<BookingOccurrence> findBySeriesIdOrderByStartTime(UUID seriesId);

    List<BookingOccurrence> findByMeetingIdOrderByStartTime(UUID meetingId);

    List<BookingOccurrence> findByRoomIdAndStartTimeBetween(UUID roomId, Instant from, Instant to);

    @Query("""
        SELECT b FROM BookingOccurrence b
        WHERE b.series.id = :seriesId
          AND b.startTime >= :from
          AND b.status = :status
        ORDER BY b.startTime
        """)
    List<BookingOccurrence> findFutureOccurrencesInSeries(
        @Param("seriesId") UUID seriesId,
        @Param("from") Instant from,
        @Param("status") BookingStatus status
    );
}
