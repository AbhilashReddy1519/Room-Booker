package com.roombooker.dto;

import com.roombooker.booking.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingOccurrenceResponse(
    UUID id,
    UUID meetingId,
    UUID seriesId,
    UUID roomId,
    String roomName,
    String title,
    Instant startTime,
    Instant endTime,
    LocalDate occurrenceDate,
    BookingStatus status,
    Instant createdAt
) {}
