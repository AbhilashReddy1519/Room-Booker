package com.roombooker.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
    UUID id,
    String name,
    Integer capacity,
    String location,
    Instant createdAt
) implements Serializable {}
