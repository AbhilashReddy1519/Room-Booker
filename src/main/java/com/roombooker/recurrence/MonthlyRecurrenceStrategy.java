package com.roombooker.recurrence;

import com.roombooker.booking.BookingSeries;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Component
public class MonthlyRecurrenceStrategy implements RecurrenceStrategy {

    private final TimezoneResolver timezoneResolver;

    public MonthlyRecurrenceStrategy(TimezoneResolver timezoneResolver) {
        this.timezoneResolver = timezoneResolver;
    }

    @Override
    public List<OccurrenceTime> generate(BookingSeries series, LocalDate horizonStart, LocalDate horizonEnd) {
        List<OccurrenceTime> result = new ArrayList<>();
        ZoneId zoneId = timezoneResolver.parseZoneId(series.getTimezone());

        LocalDate effectiveStart = series.getStartDate().isBefore(horizonStart) ? horizonStart : series.getStartDate();
        LocalDate effectiveEnd = series.getEndDate().isAfter(horizonEnd) ? horizonEnd : series.getEndDate();

        int interval = (series.getIntervalValue() != null && series.getIntervalValue() > 0) ? series.getIntervalValue() : 1;

        LocalDate baseDate = series.getStartDate();
        YearMonth currentYearMonth = YearMonth.from(baseDate);
        YearMonth endYearMonth = YearMonth.from(series.getEndDate());

        while (!currentYearMonth.isAfter(endYearMonth)) {
            LocalDate occurrenceDate = null;

            if (series.getWeekNumber() != null && series.getNthWeekday() != null) {
                // Mode B: Nth Weekday of Month (e.g. 2nd Wednesday)
                occurrenceDate = calculateNthWeekday(currentYearMonth, series.getWeekNumber(), series.getNthWeekday());
            } else {
                // Mode A: Fixed Day of Month (e.g. 15th or 31st)
                int targetDay = series.getDayOfMonth() != null ? series.getDayOfMonth() : baseDate.getDayOfMonth();
                if (targetDay <= currentYearMonth.lengthOfMonth()) {
                    occurrenceDate = currentYearMonth.atDay(targetDay);
                }
                // Note: If targetDay > lengthOfMonth (e.g. 31st in Feb), it skips the non-existent day per design
            }

            if (occurrenceDate != null
                && !occurrenceDate.isBefore(series.getStartDate())
                && !occurrenceDate.isAfter(series.getEndDate())
                && !occurrenceDate.isBefore(effectiveStart)
                && !occurrenceDate.isAfter(effectiveEnd)) {

                Instant startInstant = timezoneResolver.toInstant(occurrenceDate, series.getStartTime(), zoneId);
                Instant endInstant = timezoneResolver.toInstant(occurrenceDate, series.getEndTime(), zoneId);
                result.add(new OccurrenceTime(occurrenceDate, startInstant, endInstant));
            }

            currentYearMonth = currentYearMonth.plusMonths(interval);
        }

        return result;
    }

    private LocalDate calculateNthWeekday(YearMonth ym, int weekNumber, String nthWeekdayStr) {
        try {
            DayOfWeek dow = DayOfWeek.valueOf(nthWeekdayStr.trim().toUpperCase());
            LocalDate firstDayOfMonth = ym.atDay(1);

            if (weekNumber == 5) {
                // Last weekday of month
                return firstDayOfMonth.with(TemporalAdjusters.lastInMonth(dow));
            }

            LocalDate firstMatching = firstDayOfMonth.with(TemporalAdjusters.nextOrSame(dow));
            LocalDate nthMatching = firstMatching.plusWeeks(weekNumber - 1);

            // Ensure it remains in the target month
            if (YearMonth.from(nthMatching).equals(ym)) {
                return nthMatching;
            }
        } catch (Exception ex) {
            // Invalid parameters
        }
        return null;
    }
}
