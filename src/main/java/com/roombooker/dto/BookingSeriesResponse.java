package com.roombooker.dto;

import com.roombooker.booking.RecurrenceType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BookingSeriesResponse(
    UUID id,
    UUID meetingId,
    String timezone,
    RecurrenceType recurrenceType,
    LocalDate startDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime endTime,
    Integer intervalValue,
    String weekdays,
    Integer dayOfMonth,
    Integer weekNumber,
    String nthWeekday,
    Instant createdAt,
    List<BookingOccurrenceResponse> generatedOccurrences
) {}
