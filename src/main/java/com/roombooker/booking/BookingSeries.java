package com.roombooker.booking;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "booking_series")
public class BookingSeries {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false, unique = true)
    private Meeting meeting;

    @Column(name = "timezone", nullable = false, length = 100)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 30)
    private RecurrenceType recurrenceType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue;

    @Column(name = "weekdays", length = 100)
    private String weekdays;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "week_number")
    private Integer weekNumber;

    @Column(name = "nth_weekday", length = 20)
    private String nthWeekday;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BookingSeries() {}

    public BookingSeries(UUID id, Meeting meeting, String timezone, RecurrenceType recurrenceType, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Integer intervalValue, String weekdays, Integer dayOfMonth, Integer weekNumber, String nthWeekday, Instant createdAt) {
        this.id = id;
        this.meeting = meeting;
        this.timezone = timezone;
        this.recurrenceType = recurrenceType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.intervalValue = intervalValue;
        this.weekdays = weekdays;
        this.dayOfMonth = dayOfMonth;
        this.weekNumber = weekNumber;
        this.nthWeekday = nthWeekday;
        this.createdAt = createdAt;
    }

    public static BookingSeriesBuilder builder() {
        return new BookingSeriesBuilder();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (intervalValue == null) {
            intervalValue = 1;
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Meeting getMeeting() { return meeting; }
    public void setMeeting(Meeting meeting) { this.meeting = meeting; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getIntervalValue() { return intervalValue; }
    public void setIntervalValue(Integer intervalValue) { this.intervalValue = intervalValue; }

    public String getWeekdays() { return weekdays; }
    public void setWeekdays(String weekdays) { this.weekdays = weekdays; }

    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    public String getNthWeekday() { return nthWeekday; }
    public void setNthWeekday(String nthWeekday) { this.nthWeekday = nthWeekday; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class BookingSeriesBuilder {
        private UUID id;
        private Meeting meeting;
        private String timezone;
        private RecurrenceType recurrenceType;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer intervalValue;
        private String weekdays;
        private Integer dayOfMonth;
        private Integer weekNumber;
        private String nthWeekday;
        private Instant createdAt;

        public BookingSeriesBuilder id(UUID id) { this.id = id; return this; }
        public BookingSeriesBuilder meeting(Meeting meeting) { this.meeting = meeting; return this; }
        public BookingSeriesBuilder timezone(String timezone) { this.timezone = timezone; return this; }
        public BookingSeriesBuilder recurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; return this; }
        public BookingSeriesBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public BookingSeriesBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public BookingSeriesBuilder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public BookingSeriesBuilder endTime(LocalTime endTime) { this.endTime = endTime; return this; }
        public BookingSeriesBuilder intervalValue(Integer intervalValue) { this.intervalValue = intervalValue; return this; }
        public BookingSeriesBuilder weekdays(String weekdays) { this.weekdays = weekdays; return this; }
        public BookingSeriesBuilder dayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; return this; }
        public BookingSeriesBuilder weekNumber(Integer weekNumber) { this.weekNumber = weekNumber; return this; }
        public BookingSeriesBuilder nthWeekday(String nthWeekday) { this.nthWeekday = nthWeekday; return this; }
        public BookingSeriesBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public BookingSeries build() {
            return new BookingSeries(id, meeting, timezone, recurrenceType, startDate, endDate, startTime, endTime, intervalValue, weekdays, dayOfMonth, weekNumber, nthWeekday, createdAt);
        }
    }
}
