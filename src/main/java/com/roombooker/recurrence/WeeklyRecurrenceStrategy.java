package com.roombooker.recurrence;

import com.roombooker.booking.BookingSeries;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class WeeklyRecurrenceStrategy implements RecurrenceStrategy {

    private final TimezoneResolver timezoneResolver;

    public WeeklyRecurrenceStrategy(TimezoneResolver timezoneResolver) {
        this.timezoneResolver = timezoneResolver;
    }

    @Override
    public List<OccurrenceTime> generate(BookingSeries series, LocalDate horizonStart, LocalDate horizonEnd) {
        List<OccurrenceTime> result = new ArrayList<>();
        ZoneId zoneId = timezoneResolver.parseZoneId(series.getTimezone());

        LocalDate effectiveStart = series.getStartDate().isBefore(horizonStart) ? horizonStart : series.getStartDate();
        LocalDate effectiveEnd = series.getEndDate().isAfter(horizonEnd) ? horizonEnd : series.getEndDate();

        int interval = (series.getIntervalValue() != null && series.getIntervalValue() > 0) ? series.getIntervalValue() : 1;
        Set<DayOfWeek> targetDays = parseWeekdays(series.getWeekdays(), series.getStartDate().getDayOfWeek());

        // Base Monday of the start date week
        LocalDate baseMonday = series.getStartDate().with(DayOfWeek.MONDAY);

        LocalDate current = series.getStartDate();
        while (!current.isAfter(effectiveEnd)) {
            if (targetDays.contains(current.getDayOfWeek())) {
                LocalDate currentMonday = current.with(DayOfWeek.MONDAY);
                long weeksBetween = ChronoUnit.WEEKS.between(baseMonday, currentMonday);
                if (weeksBetween % interval == 0) {
                    if (!current.isBefore(effectiveStart)) {
                        Instant startInstant = timezoneResolver.toInstant(current, series.getStartTime(), zoneId);
                        Instant endInstant = timezoneResolver.toInstant(current, series.getEndTime(), zoneId);
                        result.add(new OccurrenceTime(current, startInstant, endInstant));
                    }
                }
            }
            current = current.plusDays(1);
        }

        return result;
    }

    private Set<DayOfWeek> parseWeekdays(String weekdaysStr, DayOfWeek defaultDay) {
        Set<DayOfWeek> set = new HashSet<>();
        if (weekdaysStr == null || weekdaysStr.isBlank()) {
            set.add(defaultDay);
            return set;
        }

        String[] tokens = weekdaysStr.split(",");
        for (String token : tokens) {
            String trimmed = token.trim().toUpperCase();
            try {
                set.add(DayOfWeek.valueOf(trimmed));
            } catch (IllegalArgumentException ex) {
                // Try 3-letter prefix match e.g. MON -> MONDAY
                for (DayOfWeek dow : DayOfWeek.values()) {
                    if (dow.name().startsWith(trimmed)) {
                        set.add(dow);
                        break;
                    }
                }
            }
        }
        if (set.isEmpty()) {
            set.add(defaultDay);
        }
        return set;
    }
}
