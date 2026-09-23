package com.roombooker.recurrence;

import com.roombooker.booking.BookingSeries;

import java.time.LocalDate;
import java.util.List;

public interface RecurrenceStrategy {
    List<OccurrenceTime> generate(BookingSeries series, LocalDate horizonStart, LocalDate horizonEnd);
}
