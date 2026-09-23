package com.roombooker.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    String code,
    String message,
    Instant timestamp,
    List<ConflictDetail> conflicts,
    List<String> details
) {
    public ApiErrorResponse(String code, String message) {
        this(code, message, Instant.now(), null, null);
    }

    public ApiErrorResponse(String code, String message, List<ConflictDetail> conflicts) {
        this(code, message, Instant.now(), conflicts, null);
    }

    public ApiErrorResponse(String code, String message, List<String> details, boolean isDetail) {
        this(code, message, Instant.now(), null, details);
    }
}
