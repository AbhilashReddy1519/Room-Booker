-- =========================================================
-- ROOMBOOKER
-- V1 - CREATE CORE TABLES
-- =========================================================


-- =========================================================
-- USERS
-- =========================================================
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN'))
);


-- =========================================================
-- ROOMS
-- =========================================================

CREATE TABLE rooms (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INTEGER NOT NULL,
    location VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rooms_capacity
        CHECK (capacity > 0)
);


-- =========================================================
-- MEETINGS
-- =========================================================
--
-- Represents the logical meeting.
--
-- Example:
--
-- "Engineering Team Meeting"
--
-- The meeting can be:
--
-- 1. One-off
-- 2. Recurring
--
-- =========================================================

CREATE TABLE meetings (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    organizer_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meetings_organizer
        FOREIGN KEY (organizer_id)
        REFERENCES users(id)
);


-- =========================================================
-- MEETING ATTENDEES
-- =========================================================
--
-- Many-to-many relationship:
--
-- One meeting -> many users
-- One user    -> many meetings
--
-- =========================================================

CREATE TABLE meeting_attendees (
    meeting_id UUID NOT NULL,
    user_id UUID NOT NULL,
    response_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (
        meeting_id,
        user_id
    ),
    CONSTRAINT fk_meeting_attendees_meeting
        FOREIGN KEY (meeting_id)
        REFERENCES meetings(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_meeting_attendees_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_meeting_attendee_response
        CHECK (
            response_status IN (
                'PENDING',
                'ACCEPTED',
                'DECLINED'
            )
        )
);


-- =========================================================
-- BOOKING SERIES
-- =========================================================
--
-- Represents the recurrence rule.
--
-- One meeting can have zero or one series.
--
-- Example:
--
-- Every Monday + Wednesday
-- 10:00 - 11:00
-- Asia/Kolkata
--
-- =========================================================

CREATE TABLE booking_series (
    id UUID PRIMARY KEY,
    meeting_id UUID NOT NULL UNIQUE,
    timezone VARCHAR(100) NOT NULL,
    recurrence_type VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    interval_value INTEGER NOT NULL DEFAULT 1,
    weekdays VARCHAR(100),
    day_of_month INTEGER,
    week_number INTEGER,
    nth_weekday VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_series_meeting
        FOREIGN KEY (meeting_id)
        REFERENCES meetings(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_booking_series_recurrence_type
        CHECK (
            recurrence_type IN (
                'DAILY',
                'WEEKLY',
                'MONTHLY'
            )
        ),
    CONSTRAINT chk_booking_series_dates
        CHECK (
            start_date <= end_date
        ),
    CONSTRAINT chk_booking_series_time
        CHECK (
            start_time < end_time
        ),
    CONSTRAINT chk_booking_series_interval
        CHECK (
            interval_value > 0
        ),
    CONSTRAINT chk_booking_series_day_of_month
        CHECK (
            day_of_month IS NULL
            OR
            day_of_month BETWEEN 1 AND 31
        ),
    CONSTRAINT chk_booking_series_week_number
        CHECK (
            week_number IS NULL
            OR
            week_number BETWEEN 1 AND 5
        )
);


-- =========================================================
-- BOOKING OCCURRENCES
-- =========================================================
--
-- Represents an actual meeting occurrence.
--
-- One-off booking:
--
--     series_id = NULL
--
-- Recurring booking:
--
--     series_id = booking_series.id
--
-- =========================================================

CREATE TABLE booking_occurrences (
    id UUID PRIMARY KEY,
    meeting_id UUID NOT NULL,
    series_id UUID,
    room_id UUID NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    occurrence_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_occurrences_meeting
        FOREIGN KEY (meeting_id)
        REFERENCES meetings(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_occurrences_series
        FOREIGN KEY (series_id)
        REFERENCES booking_series(id),
    CONSTRAINT fk_booking_occurrences_room
        FOREIGN KEY (room_id)
        REFERENCES rooms(id),
    CONSTRAINT chk_booking_occurrences_time
        CHECK (
            start_time < end_time
        ),
    CONSTRAINT chk_booking_occurrences_status
        CHECK (
            status IN (
                'CONFIRMED',
                'CANCELLED'
            )
        )
);


-- =========================================================
-- INDEXES
-- =========================================================
-- Main conflict-detection lookup.
--
-- Query will eventually look approximately like:
--
-- WHERE room_id = ?
-- AND start_time < ?
-- AND end_time > ?
--

CREATE INDEX idx_booking_occurrences_room_start
    ON booking_occurrences (
        room_id,
        start_time
    );


CREATE INDEX idx_booking_occurrences_room_end
    ON booking_occurrences (
        room_id,
        end_time
    );


-- Useful when retrieving all occurrences
-- belonging to a series.

CREATE INDEX idx_booking_occurrences_series
    ON booking_occurrences (
        series_id
    );


-- Useful when retrieving all occurrences
-- belonging to a logical meeting.

CREATE INDEX idx_booking_occurrences_meeting
    ON booking_occurrences (
        meeting_id
    );


-- Useful for calendar/date-based queries.

CREATE INDEX idx_booking_occurrences_room_date
    ON booking_occurrences (
        room_id,
        occurrence_date
    );


-- Useful for organizer-based queries.

CREATE INDEX idx_meetings_organizer
    ON meetings (
        organizer_id
    );


-- Useful when retrieving meetings
-- for an attendee.

CREATE INDEX idx_meeting_attendees_user
    ON meeting_attendees (
        user_id
    );