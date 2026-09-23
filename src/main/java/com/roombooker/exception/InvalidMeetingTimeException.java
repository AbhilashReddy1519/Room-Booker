package com.roombooker.exception;

public class InvalidMeetingTimeException extends RuntimeException {
    public InvalidMeetingTimeException(String message) {
        super(message);
    }
}
