package com.roombooker.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MeetingResponse(
    UUID id,
    String title,
    UserResponse organizer,
    List<UserResponse> attendees,
    BookingSeriesResponse series,
    List<BookingOccurrenceResponse> occurrences,
    Instant createdAt
) {}
