package com.roombooker.dto;

import com.roombooker.booking.RecurrenceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateRecurringBookingRequest(
    @NotNull(message = "Room ID is required")
    UUID roomId,

    @NotBlank(message = "Meeting title is required")
    String title,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    @NotNull(message = "End date is required")
    LocalDate endDate,

    @NotNull(message = "Start time is required")
    LocalTime startTime,

    @NotNull(message = "End time is required")
    LocalTime endTime,

    @NotBlank(message = "Timezone string is required")
    String timezone,

    @NotNull(message = "Recurrence type is required (DAILY, WEEKLY, MONTHLY)")
    RecurrenceType recurrenceType,

    @Min(value = 1, message = "Interval must be >= 1")
    Integer intervalValue,

    String weekdays,

    Integer dayOfMonth,

    Integer weekNumber,

    String nthWeekday,

    List<UUID> attendeeUserIds
) {}
