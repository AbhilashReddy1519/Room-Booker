package com.roombooker.recurrence;

import com.roombooker.booking.BookingSeries;
import com.roombooker.exception.InvalidMeetingTimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RecurrenceEngine {

    private final DailyRecurrenceStrategy dailyStrategy;
    private final WeeklyRecurrenceStrategy weeklyStrategy;
    private final MonthlyRecurrenceStrategy monthlyStrategy;
    private int maxHorizonMonths = 12;

    public RecurrenceEngine(
        DailyRecurrenceStrategy dailyStrategy,
        WeeklyRecurrenceStrategy weeklyStrategy,
        MonthlyRecurrenceStrategy monthlyStrategy
    ) {
        this(dailyStrategy, weeklyStrategy, monthlyStrategy, 12);
    }

    @Autowired
    public RecurrenceEngine(
        DailyRecurrenceStrategy dailyStrategy,
        WeeklyRecurrenceStrategy weeklyStrategy,
        MonthlyRecurrenceStrategy monthlyStrategy,
        @Value("${roombooker.recurrence.max-horizon-months:12}") int maxHorizonMonths
    ) {
        this.dailyStrategy = dailyStrategy;
        this.weeklyStrategy = weeklyStrategy;
        this.monthlyStrategy = monthlyStrategy;
        this.maxHorizonMonths = maxHorizonMonths > 0 ? maxHorizonMonths : 12;
    }

    public List<OccurrenceTime> generateOccurrences(BookingSeries series) {
        return generateOccurrences(series, series.getStartDate(), series.getEndDate());
    }

    public List<OccurrenceTime> generateOccurrences(BookingSeries series, LocalDate horizonStart, LocalDate horizonEnd) {
        validateHorizon(series.getStartDate(), series.getEndDate());

        RecurrenceStrategy strategy = switch (series.getRecurrenceType()) {
            case DAILY -> dailyStrategy;
            case WEEKLY -> weeklyStrategy;
            case MONTHLY -> monthlyStrategy;
        };

        return strategy.generate(series, horizonStart, horizonEnd);
    }

    private void validateHorizon(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new InvalidMeetingTimeException("Series endDate (" + end + ") cannot be before startDate (" + start + ").");
        }
        LocalDate maxAllowedEnd = start.plusMonths(maxHorizonMonths);
        if (end.isAfter(maxAllowedEnd)) {
            throw new InvalidMeetingTimeException(
                String.format("Series recurrence horizon exceeds maximum allowed limit of %d months (%s).", maxHorizonMonths, maxAllowedEnd)
            );
        }
    }
}
