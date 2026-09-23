package com.roombooker.dto;

import com.roombooker.booking.RecurrenceType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateSeriesRequest(
    @NotNull(message = "Scope is required (THIS, THIS_AND_FUTURE, WHOLE_SERIES)")
    UpdateSeriesScope scope,

    LocalDate targetOccurrenceDate,

    UUID roomId,

    String title,

    LocalTime startTime,

    LocalTime endTime,

    LocalDate newEndDate,

    RecurrenceType recurrenceType,

    Integer intervalValue,

    String weekdays,

    Integer dayOfMonth,

    Integer weekNumber,

    String nthWeekday
) {}
