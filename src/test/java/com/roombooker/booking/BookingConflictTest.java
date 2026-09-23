package com.roombooker.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BookingConflictTest {

    private boolean isOverlapping(Instant existStart, Instant existEnd, Instant reqStart, Instant reqEnd) {
        return existStart.isBefore(reqEnd) && existEnd.isAfter(reqStart);
    }

    @Test
    @DisplayName("Exact Overlap — Same time slot conflicts")
    void testExactOverlap() {
        Instant eStart = Instant.parse("2026-10-10T10:00:00Z");
        Instant eEnd   = Instant.parse("2026-10-10T11:00:00Z");

        Instant rStart = Instant.parse("2026-10-10T10:00:00Z");
        Instant rEnd   = Instant.parse("2026-10-10T11:00:00Z");

        assertTrue(isOverlapping(eStart, eEnd, rStart, rEnd));
    }

    @Test
    @DisplayName("Partial Overlap — Request starts inside existing booking")
    void testPartialOverlap() {
        Instant eStart = Instant.parse("2026-10-10T10:00:00Z");
        Instant eEnd   = Instant.parse("2026-10-10T11:00:00Z");

        Instant rStart = Instant.parse("2026-10-10T10:30:00Z");
        Instant rEnd   = Instant.parse("2026-10-10T11:30:00Z");

        assertTrue(isOverlapping(eStart, eEnd, rStart, rEnd));
    }

    @Test
    @DisplayName("Adjacent Booking — Request starts exactly when existing ends (No Conflict)")
    void testAdjacentBookingNoConflict() {
        Instant eStart = Instant.parse("2026-10-10T10:00:00Z");
        Instant eEnd   = Instant.parse("2026-10-10T11:00:00Z");

        Instant rStart = Instant.parse("2026-10-10T11:00:00Z");
        Instant rEnd   = Instant.parse("2026-10-10T12:00:00Z");

        assertFalse(isOverlapping(eStart, eEnd, rStart, rEnd));
    }
}
