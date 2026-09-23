package com.roombooker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateOneOffBookingRequest(
    @NotNull(message = "Room ID is required")
    UUID roomId,

    @NotBlank(message = "Meeting title is required")
    String title,

    @NotNull(message = "Start time (UTC Instant) is required")
    Instant startTime,

    @NotNull(message = "End time (UTC Instant) is required")
    Instant endTime,

    List<UUID> attendeeUserIds
) {}
