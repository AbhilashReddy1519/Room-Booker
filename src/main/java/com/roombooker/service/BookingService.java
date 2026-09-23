package com.roombooker.service;

import com.roombooker.booking.*;
import com.roombooker.dto.*;
import com.roombooker.exception.*;
import com.roombooker.recurrence.OccurrenceTime;
import com.roombooker.recurrence.RecurrenceEngine;
import com.roombooker.recurrence.TimezoneResolver;
import com.roombooker.repository.*;
import com.roombooker.rooms.Room;
import com.roombooker.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final MeetingRepository meetingRepository;
    private final MeetingAttendeeRepository meetingAttendeeRepository;
    private final BookingSeriesRepository bookingSeriesRepository;
    private final BookingOccurrenceRepository bookingOccurrenceRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RecurrenceEngine recurrenceEngine;
    private final TimezoneResolver timezoneResolver;
    private final AuthService authService;

    public BookingService(
        MeetingRepository meetingRepository,
        MeetingAttendeeRepository meetingAttendeeRepository,
        BookingSeriesRepository bookingSeriesRepository,
        BookingOccurrenceRepository bookingOccurrenceRepository,
        RoomRepository roomRepository,
        UserRepository userRepository,
        RecurrenceEngine recurrenceEngine,
        TimezoneResolver timezoneResolver,
        AuthService authService
    ) {
        this.meetingRepository = meetingRepository;
        this.meetingAttendeeRepository = meetingAttendeeRepository;
        this.bookingSeriesRepository = bookingSeriesRepository;
        this.bookingOccurrenceRepository = bookingOccurrenceRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.recurrenceEngine = recurrenceEngine;
        this.timezoneResolver = timezoneResolver;
        this.authService = authService;
    }

    @Transactional
    public MeetingResponse createOneOffBooking(UUID organizerId, CreateOneOffBookingRequest request) {
        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime())) {
            throw new InvalidMeetingTimeException("Start time must be strictly before end time.");
        }

        Room room = roomRepository.findById(request.roomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.roomId()));

        User organizer = userRepository.findById(organizerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + organizerId));

        // Overlap query
        List<BookingOccurrence> conflicts = bookingOccurrenceRepository.findConflicts(
            room.getId(),
            request.startTime(),
            request.endTime(),
            BookingStatus.CONFIRMED
        );

        if (!conflicts.isEmpty()) {
            List<ConflictDetail> details = conflicts.stream()
                .map(c -> new ConflictDetail(
                    c.getOccurrenceDate(),
                    request.startTime(),
                    request.endTime(),
                    c.getId(),
                    c.getMeeting().getTitle(),
                    c.getStartTime(),
                    c.getEndTime()
                ))
                .collect(Collectors.toList());
            throw new BookingConflictException("Room is unavailable for requested time slot", details);
        }

        Meeting meeting = Meeting.builder()
            .title(request.title())
            .organizer(organizer)
            .build();
        Meeting savedMeeting = meetingRepository.save(meeting);

        List<UserResponse> attendeeResponses = saveAttendees(savedMeeting, request.attendeeUserIds());

        LocalDate occurrenceDate = LocalDate.ofInstant(request.startTime(), ZoneOffset.UTC);

        BookingOccurrence occurrence = BookingOccurrence.builder()
            .meeting(savedMeeting)
            .series(null)
            .room(room)
            .startTime(request.startTime())
            .endTime(request.endTime())
            .occurrenceDate(occurrenceDate)
            .status(BookingStatus.CONFIRMED)
            .build();

        BookingOccurrence savedOccurrence = bookingOccurrenceRepository.save(occurrence);

        BookingOccurrenceResponse occurrenceResp = toOccurrenceResponse(savedOccurrence);
        UserResponse organizerResp = authService.toUserResponse(organizer);

        return new MeetingResponse(
            savedMeeting.getId(),
            savedMeeting.getTitle(),
            organizerResp,
            attendeeResponses,
            null,
            List.of(occurrenceResp),
            savedMeeting.getCreatedAt()
        );
    }

    @Transactional
    public MeetingResponse createRecurringBooking(UUID organizerId, CreateRecurringBookingRequest request) {
        Room room = roomRepository.findById(request.roomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.roomId()));

        User organizer = userRepository.findById(organizerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + organizerId));

        Meeting meeting = Meeting.builder()
            .title(request.title())
            .organizer(organizer)
            .build();
        Meeting savedMeeting = meetingRepository.save(meeting);

        List<UserResponse> attendeeResponses = saveAttendees(savedMeeting, request.attendeeUserIds());

        BookingSeries series = BookingSeries.builder()
            .meeting(savedMeeting)
            .timezone(request.timezone())
            .recurrenceType(request.recurrenceType())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .intervalValue(request.intervalValue() != null ? request.intervalValue() : 1)
            .weekdays(request.weekdays())
            .dayOfMonth(request.dayOfMonth())
            .weekNumber(request.weekNumber())
            .nthWeekday(request.nthWeekday())
            .build();

        BookingSeries savedSeries = bookingSeriesRepository.save(series);

        List<OccurrenceTime> generatedTimes = recurrenceEngine.generateOccurrences(savedSeries);

        List<ConflictDetail> conflictsFound = new ArrayList<>();
        List<BookingOccurrence> occurrencesToSave = new ArrayList<>();

        for (OccurrenceTime ot : generatedTimes) {
            List<BookingOccurrence> existingConflicts = bookingOccurrenceRepository.findConflicts(
                room.getId(),
                ot.startTime(),
                ot.endTime(),
                BookingStatus.CONFIRMED
            );

            if (!existingConflicts.isEmpty()) {
                for (BookingOccurrence ex : existingConflicts) {
                    conflictsFound.add(new ConflictDetail(
                        ot.date(),
                        ot.startTime(),
                        ot.endTime(),
                        ex.getId(),
                        ex.getMeeting().getTitle(),
                        ex.getStartTime(),
                        ex.getEndTime()
                    ));
                }
            } else {
                occurrencesToSave.add(BookingOccurrence.builder()
                    .meeting(savedMeeting)
                    .series(savedSeries)
                    .room(room)
                    .startTime(ot.startTime())
                    .endTime(ot.endTime())
                    .occurrenceDate(ot.date())
                    .status(BookingStatus.CONFIRMED)
                    .build());
            }
        }

        if (!conflictsFound.isEmpty()) {
            // Rollback whole recurring series creation
            throw new BookingConflictException("Recurring booking conflicts with existing meetings", conflictsFound);
        }

        List<BookingOccurrence> savedOccurrences = bookingOccurrenceRepository.saveAll(occurrencesToSave);

        List<BookingOccurrenceResponse> occurrenceResponses = savedOccurrences.stream()
            .map(this::toOccurrenceResponse)
            .collect(Collectors.toList());

        BookingSeriesResponse seriesResp = toSeriesResponse(savedSeries, occurrenceResponses);
        UserResponse organizerResp = authService.toUserResponse(organizer);

        return new MeetingResponse(
            savedMeeting.getId(),
            savedMeeting.getTitle(),
            organizerResp,
            attendeeResponses,
            seriesResp,
            occurrenceResponses,
            savedMeeting.getCreatedAt()
        );
    }

    @Transactional
    public BookingSeriesResponse updateSeries(UUID seriesId, UpdateSeriesRequest request) {
        BookingSeries series = bookingSeriesRepository.findById(seriesId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking series not found with ID: " + seriesId));

        switch (request.scope()) {
            case THIS -> {
                if (request.targetOccurrenceDate() == null) {
                    throw new InvalidMeetingTimeException("targetOccurrenceDate is required for scope THIS");
                }
                List<BookingOccurrence> occurrences = bookingOccurrenceRepository.findBySeriesIdOrderByStartTime(seriesId);
                BookingOccurrence target = occurrences.stream()
                    .filter(o -> o.getOccurrenceDate().equals(request.targetOccurrenceDate()) && o.getStatus() == BookingStatus.CONFIRMED)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Occurrence not found for date: " + request.targetOccurrenceDate()));

                if (request.roomId() != null) {
                    Room room = roomRepository.findById(request.roomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.roomId()));
                    target.setRoom(room);
                }

                if (request.startTime() != null || request.endTime() != null) {
                    ZoneId zoneId = timezoneResolver.parseZoneId(series.getTimezone());
                    var newStart = request.startTime() != null ? request.startTime() : series.getStartTime();
                    var newEnd = request.endTime() != null ? request.endTime() : series.getEndTime();
                    target.setStartTime(timezoneResolver.toInstant(target.getOccurrenceDate(), newStart, zoneId));
                    target.setEndTime(timezoneResolver.toInstant(target.getOccurrenceDate(), newEnd, zoneId));
                }

                bookingOccurrenceRepository.save(target);
            }
            case THIS_AND_FUTURE -> {
                if (request.targetOccurrenceDate() == null) {
                    throw new InvalidMeetingTimeException("targetOccurrenceDate is required for scope THIS_AND_FUTURE");
                }
                LocalDate splitDate = request.targetOccurrenceDate();

                // 1. Truncate old series
                series.setEndDate(splitDate.minusDays(1));
                bookingSeriesRepository.save(series);

                // 2. Cancel future occurrences of old series
                ZoneId zoneId = timezoneResolver.parseZoneId(series.getTimezone());
                Instant splitInstant = timezoneResolver.toInstant(splitDate, series.getStartTime(), zoneId);
                List<BookingOccurrence> futureOccurrences = bookingOccurrenceRepository.findFutureOccurrencesInSeries(
                    seriesId, splitInstant, BookingStatus.CONFIRMED
                );
                for (BookingOccurrence bo : futureOccurrences) {
                    bo.setStatus(BookingStatus.CANCELLED);
                }
                bookingOccurrenceRepository.saveAll(futureOccurrences);

                // 3. Create new series starting at targetOccurrenceDate
                LocalDate newEnd = request.newEndDate() != null ? request.newEndDate() : series.getEndDate().plusMonths(3);
                Meeting oldMeeting = series.getMeeting();

                BookingSeries newSeries = BookingSeries.builder()
                    .meeting(oldMeeting)
                    .timezone(series.getTimezone())
                    .recurrenceType(request.recurrenceType() != null ? request.recurrenceType() : series.getRecurrenceType())
                    .startDate(splitDate)
                    .endDate(newEnd)
                    .startTime(request.startTime() != null ? request.startTime() : series.getStartTime())
                    .endTime(request.endTime() != null ? request.endTime() : series.getEndTime())
                    .intervalValue(request.intervalValue() != null ? request.intervalValue() : series.getIntervalValue())
                    .weekdays(request.weekdays() != null ? request.weekdays() : series.getWeekdays())
                    .dayOfMonth(request.dayOfMonth() != null ? request.dayOfMonth() : series.getDayOfMonth())
                    .weekNumber(request.weekNumber() != null ? request.weekNumber() : series.getWeekNumber())
                    .nthWeekday(request.nthWeekday() != null ? request.nthWeekday() : series.getNthWeekday())
                    .build();

                BookingSeries savedNewSeries = bookingSeriesRepository.save(newSeries);
                Room targetRoom;
                if (request.roomId() != null) {
                    targetRoom = roomRepository.findById(request.roomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.roomId()));
                } else if (!futureOccurrences.isEmpty()) {
                    targetRoom = futureOccurrences.get(0).getRoom();
                } else {
                    throw new InvalidMeetingTimeException(
                        "roomId is required when editing a series with no existing future occurrences to infer it from.");
                }

                List<OccurrenceTime> newTimes = recurrenceEngine.generateOccurrences(savedNewSeries);
                List<BookingOccurrence> newOccurrences = new ArrayList<>();

                for (OccurrenceTime ot : newTimes) {
                    newOccurrences.add(BookingOccurrence.builder()
                        .meeting(oldMeeting)
                        .series(savedNewSeries)
                        .room(targetRoom)
                        .startTime(ot.startTime())
                        .endTime(ot.endTime())
                        .occurrenceDate(ot.date())
                        .status(BookingStatus.CONFIRMED)
                        .build());
                }
                bookingOccurrenceRepository.saveAll(newOccurrences);
            }
            case WHOLE_SERIES -> {
                // Cancel existing occurrences
                List<BookingOccurrence> existing = bookingOccurrenceRepository.findBySeriesIdOrderByStartTime(seriesId);
                for (BookingOccurrence bo : existing) {
                    bo.setStatus(BookingStatus.CANCELLED);
                }
                bookingOccurrenceRepository.saveAll(existing);

                // Update series
                if (request.newEndDate() != null) series.setEndDate(request.newEndDate());
                if (request.startTime() != null) series.setStartTime(request.startTime());
                if (request.endTime() != null) series.setEndTime(request.endTime());
                if (request.recurrenceType() != null) series.setRecurrenceType(request.recurrenceType());
                if (request.intervalValue() != null) series.setIntervalValue(request.intervalValue());
                if (request.weekdays() != null) series.setWeekdays(request.weekdays());

                BookingSeries saved = bookingSeriesRepository.save(series);
                Room room;
                if (request.roomId() != null) {
                    room = roomRepository.findById(request.roomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.roomId()));
                } else if (!existing.isEmpty()) {
                    room = existing.get(0).getRoom();
                } else {
                    throw new InvalidMeetingTimeException(
                        "roomId is required when editing a series with no existing occurrences to infer it from.");
                }

                List<OccurrenceTime> newTimes = recurrenceEngine.generateOccurrences(saved);
                List<BookingOccurrence> regenerated = new ArrayList<>();
                for (OccurrenceTime ot : newTimes) {
                    regenerated.add(BookingOccurrence.builder()
                        .meeting(series.getMeeting())
                        .series(saved)
                        .room(room)
                        .startTime(ot.startTime())
                        .endTime(ot.endTime())
                        .occurrenceDate(ot.date())
                        .status(BookingStatus.CONFIRMED)
                        .build());
                }
                bookingOccurrenceRepository.saveAll(regenerated);
            }
        }

        BookingSeries finalSeries = bookingSeriesRepository.findById(seriesId).orElseThrow();
        List<BookingOccurrenceResponse> finalOccurrences = bookingOccurrenceRepository.findBySeriesIdOrderByStartTime(seriesId).stream()
            .map(this::toOccurrenceResponse)
            .collect(Collectors.toList());

        return toSeriesResponse(finalSeries, finalOccurrences);
    }

    @Transactional
    public void cancelOccurrence(UUID occurrenceId) {
        BookingOccurrence occurrence = bookingOccurrenceRepository.findById(occurrenceId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking occurrence not found with ID: " + occurrenceId));

        occurrence.setStatus(BookingStatus.CANCELLED);
        bookingOccurrenceRepository.save(occurrence);
    }

    @Transactional
    public void cancelSeries(UUID seriesId) {
        BookingSeries series = bookingSeriesRepository.findById(seriesId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking series not found with ID: " + seriesId));

        List<BookingOccurrence> occurrences = bookingOccurrenceRepository.findBySeriesIdOrderByStartTime(series.getId());
        for (BookingOccurrence bo : occurrences) {
            bo.setStatus(BookingStatus.CANCELLED);
        }
        bookingOccurrenceRepository.saveAll(occurrences);
    }

    @Transactional(readOnly = true)
    public List<BookingOccurrenceResponse> getOccurrencesByRoomAndDateRange(UUID roomId, Instant from, Instant to) {
        return bookingOccurrenceRepository.findByRoomIdAndStartTimeBetween(roomId, from, to).stream()
            .map(this::toOccurrenceResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MeetingResponse getMeetingById(UUID meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with ID: " + meetingId));

        List<MeetingAttendee> attendees = meetingAttendeeRepository.findByMeetingId(meetingId);
        List<UserResponse> attendeeResponses = attendees.stream()
            .map(ma -> authService.toUserResponse(ma.getUser()))
            .collect(Collectors.toList());

        Optional<BookingSeries> seriesOpt = bookingSeriesRepository.findByMeetingId(meetingId);
        List<BookingOccurrence> occurrences = bookingOccurrenceRepository.findByMeetingIdOrderByStartTime(meetingId);

        List<BookingOccurrenceResponse> occurrenceResponses = occurrences.stream()
            .map(this::toOccurrenceResponse)
            .collect(Collectors.toList());

        BookingSeriesResponse seriesResponse = seriesOpt.map(s -> toSeriesResponse(s, occurrenceResponses)).orElse(null);
        UserResponse organizerResp = authService.toUserResponse(meeting.getOrganizer());

        return new MeetingResponse(
            meeting.getId(),
            meeting.getTitle(),
            organizerResp,
            attendeeResponses,
            seriesResponse,
            occurrenceResponses,
            meeting.getCreatedAt()
        );
    }

    private List<UserResponse> saveAttendees(Meeting meeting, List<UUID> attendeeUserIds) {
        if (attendeeUserIds == null || attendeeUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserResponse> responses = new ArrayList<>();
        for (UUID userId : attendeeUserIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                MeetingAttendeeId id = new MeetingAttendeeId(meeting.getId(), user.getId());
                MeetingAttendee ma = MeetingAttendee.builder()
                    .id(id)
                    .meeting(meeting)
                    .user(user)
                    .responseStatus(AttendeeResponseStatus.PENDING)
                    .build();
                meetingAttendeeRepository.save(ma);
                responses.add(authService.toUserResponse(user));
            }
        }
        return responses;
    }

    private BookingOccurrenceResponse toOccurrenceResponse(BookingOccurrence bo) {
        return new BookingOccurrenceResponse(
            bo.getId(),
            bo.getMeeting().getId(),
            bo.getSeries() != null ? bo.getSeries().getId() : null,
            bo.getRoom().getId(),
            bo.getRoom().getName(),
            bo.getMeeting().getTitle(),
            bo.getStartTime(),
            bo.getEndTime(),
            bo.getOccurrenceDate(),
            bo.getStatus(),
            bo.getCreatedAt()
        );
    }

    private BookingSeriesResponse toSeriesResponse(BookingSeries bs, List<BookingOccurrenceResponse> occurrences) {
        return new BookingSeriesResponse(
            bs.getId(),
            bs.getMeeting().getId(),
            bs.getTimezone(),
            bs.getRecurrenceType(),
            bs.getStartDate(),
            bs.getEndDate(),
            bs.getStartTime(),
            bs.getEndTime(),
            bs.getIntervalValue(),
            bs.getWeekdays(),
            bs.getDayOfMonth(),
            bs.getWeekNumber(),
            bs.getNthWeekday(),
            bs.getCreatedAt(),
            occurrences
        );
    }
}
