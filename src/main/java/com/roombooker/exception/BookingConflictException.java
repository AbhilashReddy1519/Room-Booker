package com.roombooker.exception;

import java.util.Collections;
import java.util.List;

public class BookingConflictException extends RuntimeException {

    private final List<ConflictDetail> conflicts;

    public BookingConflictException(String message) {
        super(message);
        this.conflicts = Collections.emptyList();
    }

    public BookingConflictException(String message, List<ConflictDetail> conflicts) {
        super(message);
        this.conflicts = conflicts;
    }

    public List<ConflictDetail> getConflicts() {
        return conflicts;
    }
}
