# Architecture Specification — RoomBooker

## System Architecture Overview

```
 ┌─────────────────────────────────────────────────────────────┐
 │                        HTTP / REST                          │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                    Spring Security + JWT                    │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                      Booking Controller                     │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                       Booking Service                       │
 └──────┬───────────────────────┬──────────────────────┬───────┘
        │                       │                      │
        ▼                       ▼                      ▼
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Recurrence  │     │ TimezoneResolver │     │ Conflict Engine  │
│    Engine    │     │    (DST Check)   │     │ (Interval Query) │
└───────┬──────┘     └──────────────────┘     └────────┬─────────┘
        │                                              │
        └───────────────────────┬──────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │               PostgreSQL 17 (Flyway Migrations)             │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                     Redis 7 Metadata Cache                  │
 └─────────────────────────────────────────────────────────────┘
```

---

## Technical Design Decisions & Trade-Offs

### 1. Materialized Occurrences vs Dynamic Calculation
- **Decision**: Occurrences are expanded and materialized into `booking_occurrences` up to a bounded horizon of **12 months**.
- **Rationale**: Materialization makes overlap checking, series editing (`THIS`, `THIS_AND_FUTURE`), and calendar queries $O(k \log n)$ rather than scanning every recurrence rule dynamically.

### 2. Timezone & DST Persistence Strategy
- **Decision**: Local calendar date, start time, end time, and ZoneId are stored on `booking_series`. Every individual occurrence is converted into an absolute UTC `Instant` and stored in `booking_occurrences`.
- **Rationale**: Prevents clock skew and DST transition corruption. Recurrence logic evaluates in local time while persistence occurs in UTC.

### 3. Graceful Redis Degradation
- **Decision**: Redis caches room metadata (`@Cacheable(value="rooms")`).
- **Rationale**: If Redis crashes or experiences network issues, room lookups fallback directly to PostgreSQL without disrupting core booking creation.
