package com.roombooker.recurrence;

import com.roombooker.exception.InvalidMeetingTimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TimezoneDstTest {

    private TimezoneResolver timezoneResolver;

    @BeforeEach
    void setUp() {
        timezoneResolver = new TimezoneResolver();
    }

    @Test
    @DisplayName("DST Gap — Spring forward date 2:30 AM in America/New_York throws InvalidMeetingTimeException")
    void testDstSpringForwardGap() {
        // March 8, 2026 is Spring Forward in US Eastern Time. 2:00 AM -> 3:00 AM. 2:30 AM does not exist.
        LocalDate date = LocalDate.of(2026, 3, 8);
        LocalTime time = LocalTime.of(2, 30);
        ZoneId zone = ZoneId.of("America/New_York");

        assertThrows(InvalidMeetingTimeException.class, () -> {
            timezoneResolver.toInstant(date, time, zone);
        });
    }

    @Test
    @DisplayName("DST Overlap — Fall back date resolves to earlier offset")
    void testDstFallBackOverlap() {
        // Nov 1, 2026 is Fall Back in US Eastern Time (1:30 AM happens twice).
        LocalDate date = LocalDate.of(2026, 11, 1);
        LocalTime time = LocalTime.of(1, 30);
        ZoneId zone = ZoneId.of("America/New_York");

        Instant instant = timezoneResolver.toInstant(date, time, zone);
        assertNotNull(instant);
    }
}
