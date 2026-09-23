package com.roombooker.recurrence;

import com.roombooker.booking.BookingSeries;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class DailyRecurrenceStrategy implements RecurrenceStrategy {

    private final TimezoneResolver timezoneResolver;

    public DailyRecurrenceStrategy(TimezoneResolver timezoneResolver) {
        this.timezoneResolver = timezoneResolver;
    }

    @Override
    public List<OccurrenceTime> generate(BookingSeries series, LocalDate horizonStart, LocalDate horizonEnd) {
        List<OccurrenceTime> result = new ArrayList<>();
        ZoneId zoneId = timezoneResolver.parseZoneId(series.getTimezone());

        LocalDate effectiveStart = series.getStartDate().isBefore(horizonStart) ? horizonStart : series.getStartDate();
        LocalDate effectiveEnd = series.getEndDate().isAfter(horizonEnd) ? horizonEnd : series.getEndDate();

        int interval = (series.getIntervalValue() != null && series.getIntervalValue() > 0) ? series.getIntervalValue() : 1;

        LocalDate current = series.getStartDate();
        while (!current.isAfter(effectiveEnd)) {
            if (!current.isBefore(effectiveStart)) {
                Instant startInstant = timezoneResolver.toInstant(current, series.getStartTime(), zoneId);
                Instant endInstant = timezoneResolver.toInstant(current, series.getEndTime(), zoneId);
                result.add(new OccurrenceTime(current, startInstant, endInstant));
            }
            current = current.plusDays(interval);
        }

        return result;
    }
}
