package com.roombooker.dto;

import com.roombooker.users.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    UserRole role,
    Instant createdAt
) {}
