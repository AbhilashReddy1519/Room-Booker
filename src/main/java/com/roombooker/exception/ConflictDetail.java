package com.roombooker.exception;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ConflictDetail(
    LocalDate date,
    Instant requestedStart,
    Instant requestedEnd,
    UUID existingBookingId,
    String existingMeetingTitle,
    Instant existingStart,
    Instant existingEnd
) {}
