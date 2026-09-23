package com.roombooker.controller;

import com.roombooker.dto.*;
import com.roombooker.repository.UserRepository;
import com.roombooker.service.BookingService;
import com.roombooker.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Endpoints for One-Off & Recurring Meeting Room Bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Create one-off room booking", description = "Books a room for a single time slot [startTime, endTime). Rejects if overlapping.")
    public ResponseEntity<MeetingResponse> createOneOffBooking(@Valid @RequestBody CreateOneOffBookingRequest request) {
        UUID organizerId = getCurrentUserId();
        MeetingResponse response = bookingService.createOneOffBooking(organizerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/recurring")
    @Operation(summary = "Create recurring booking series", description = "Expands daily/weekly/monthly recurrence, checks conflicts, and creates series atomically.")
    public ResponseEntity<MeetingResponse> createRecurringBooking(@Valid @RequestBody CreateRecurringBookingRequest request) {
        UUID organizerId = getCurrentUserId();
        MeetingResponse response = bookingService.createRecurringBooking(organizerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meetings/{id}")
    @Operation(summary = "Get meeting details", description = "Fetches a meeting with its attendees, recurrence series, and occurrences.")
    public ResponseEntity<MeetingResponse> getMeetingById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getMeetingById(id));
    }

    @GetMapping("/occurrences")
    @Operation(summary = "Search occurrences by room & date range", description = "Returns active occurrences for a given room between two UTC Instants.")
    public ResponseEntity<List<BookingOccurrenceResponse>> getOccurrences(
        @RequestParam UUID roomId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(bookingService.getOccurrencesByRoomAndDateRange(roomId, from, to));
    }

    @PatchMapping("/series/{seriesId}")
    @Operation(summary = "Edit recurring series", description = "Edits series occurrences with scope THIS, THIS_AND_FUTURE, or WHOLE_SERIES.")
    public ResponseEntity<BookingSeriesResponse> updateSeries(
        @PathVariable UUID seriesId,
        @Valid @RequestBody UpdateSeriesRequest request
    ) {
        BookingSeriesResponse response = bookingService.updateSeries(seriesId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/occurrences/{id}")
    @Operation(summary = "Cancel single occurrence", description = "Cancels a single occurrence instance of a meeting.")
    public ResponseEntity<Void> cancelOccurrence(@PathVariable UUID id) {
        bookingService.cancelOccurrence(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/series/{seriesId}")
    @Operation(summary = "Cancel entire recurring series", description = "Cancels all occurrences belonging to a recurrence series.")
    public ResponseEntity<Void> cancelSeries(@PathVariable UUID seriesId) {
        bookingService.cancelSeries(seriesId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                return user.getId();
            }
        }
        throw new IllegalStateException("Authenticated user context not found");
    }
}
