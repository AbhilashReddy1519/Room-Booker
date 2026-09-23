package com.roombooker.recurrence;

import java.time.Instant;
import java.time.LocalDate;

public record OccurrenceTime(
    LocalDate date,
    Instant startTime,
    Instant endTime
) {}
