package com.roombooker.recurrence;

import com.roombooker.booking.BookingSeries;
import com.roombooker.booking.RecurrenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceEngineTest {

    private RecurrenceEngine recurrenceEngine;

    @BeforeEach
    void setUp() {
        TimezoneResolver timezoneResolver = new TimezoneResolver();
        DailyRecurrenceStrategy dailyStrategy = new DailyRecurrenceStrategy(timezoneResolver);
        WeeklyRecurrenceStrategy weeklyStrategy = new WeeklyRecurrenceStrategy(timezoneResolver);
        MonthlyRecurrenceStrategy monthlyStrategy = new MonthlyRecurrenceStrategy(timezoneResolver);
        recurrenceEngine = new RecurrenceEngine(dailyStrategy, weeklyStrategy, monthlyStrategy);
    }

    @Test
    @DisplayName("Daily Recurrence — Every 2 days for 10 days")
    void testDailyRecurrence() {
        BookingSeries series = BookingSeries.builder()
            .timezone("UTC")
            .recurrenceType(RecurrenceType.DAILY)
            .startDate(LocalDate.of(2026, 10, 1))
            .endDate(LocalDate.of(2026, 10, 10))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(11, 0))
            .intervalValue(2)
            .build();

        List<OccurrenceTime> occurrences = recurrenceEngine.generateOccurrences(series);

        // Dates: Oct 1, 3, 5, 7, 9 -> 5 occurrences
        assertEquals(5, occurrences.size());
        assertEquals(LocalDate.of(2026, 10, 1), occurrences.get(0).date());
        assertEquals(LocalDate.of(2026, 10, 9), occurrences.get(4).date());
    }

    @Test
    @DisplayName("Weekly Recurrence — Every Monday and Wednesday")
    void testWeeklyRecurrenceMultiDay() {
        BookingSeries series = BookingSeries.builder()
            .timezone("Asia/Kolkata")
            .recurrenceType(RecurrenceType.WEEKLY)
            .startDate(LocalDate.of(2026, 10, 5)) // Monday
            .endDate(LocalDate.of(2026, 10, 18))  // Sunday (2 full weeks)
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(15, 0))
            .intervalValue(1)
            .weekdays("MONDAY,WEDNESDAY")
            .build();

        List<OccurrenceTime> occurrences = recurrenceEngine.generateOccurrences(series);

        // Week 1: Oct 5 (Mon), Oct 7 (Wed); Week 2: Oct 12 (Mon), Oct 14 (Wed) -> 4 occurrences
        assertEquals(4, occurrences.size());
        assertEquals(LocalDate.of(2026, 10, 5), occurrences.get(0).date());
        assertEquals(LocalDate.of(2026, 10, 7), occurrences.get(1).date());
        assertEquals(LocalDate.of(2026, 10, 12), occurrences.get(2).date());
        assertEquals(LocalDate.of(2026, 10, 14), occurrences.get(3).date());
    }

    @Test
    @DisplayName("Monthly Recurrence — 31st of month skips short February")
    void testMonthlyShortMonthSkipping() {
        BookingSeries series = BookingSeries.builder()
            .timezone("UTC")
            .recurrenceType(RecurrenceType.MONTHLY)
            .startDate(LocalDate.of(2026, 1, 31))
            .endDate(LocalDate.of(2026, 4, 30))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .intervalValue(1)
            .dayOfMonth(31)
            .build();

        List<OccurrenceTime> occurrences = recurrenceEngine.generateOccurrences(series);

        // Jan 31 (31st), Feb (skips), Mar 31 (31st), Apr (skips) -> 2 occurrences
        assertEquals(2, occurrences.size());
        assertEquals(LocalDate.of(2026, 1, 31), occurrences.get(0).date());
        assertEquals(LocalDate.of(2026, 3, 31), occurrences.get(1).date());
    }

    @Test
    @DisplayName("Monthly Recurrence — 2nd Wednesday of every month")
    void testMonthlyNthWeekday() {
        BookingSeries series = BookingSeries.builder()
            .timezone("UTC")
            .recurrenceType(RecurrenceType.MONTHLY)
            .startDate(LocalDate.of(2026, 10, 1))
            .endDate(LocalDate.of(2026, 12, 31))
            .startTime(LocalTime.of(16, 0))
            .endTime(LocalTime.of(17, 0))
            .intervalValue(1)
            .weekNumber(2)
            .nthWeekday("WEDNESDAY")
            .build();

        List<OccurrenceTime> occurrences = recurrenceEngine.generateOccurrences(series);

        assertEquals(3, occurrences.size());
        // Oct 2026 1st is Thu -> 1st Wed is Oct 7 -> 2nd Wed is Oct 14
        assertEquals(LocalDate.of(2026, 10, 14), occurrences.get(0).date());
        // Nov 2026 1st is Sun -> 1st Wed is Nov 4 -> 2nd Wed is Nov 11
        assertEquals(LocalDate.of(2026, 11, 11), occurrences.get(1).date());
        // Dec 2026 1st is Tue -> 1st Wed is Dec 2 -> 2nd Wed is Dec 9
        assertEquals(LocalDate.of(2026, 12, 9), occurrences.get(2).date());
    }
}
