# RoomBooker — Recurring Meeting Room Booking System

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL 17](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Redis 7](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

Production-grade backend service built with **Java 21, Spring Boot 3, PostgreSQL, Redis, Flyway, and Docker** for managing meeting room reservations, supporting **one-off bookings, recurring bookings (Daily, Weekly, Monthly date/nth-weekday), timezone/DST-aware occurrence expansion, $O(k \log n)$ conflict detection, and series editing (`THIS`, `THIS_AND_FUTURE`, `WHOLE_SERIES`)**.

---

## Key Features

- **JWT Authentication & Authorization**: Role-based access control (`USER`, `ADMIN`) using stateless Spring Security + JWT.
- **One-Off & Recurring Bookings**: Full support for single reservations and recurrence rules (Daily, Weekly multi-day, Monthly fixed-day, Monthly Nth-weekday).
- **Timezone & DST Correctness**: Stores occurrences as absolute UTC `Instant` values while generating recurrence in the user's local `ZoneId`. Automatically detects and rejects DST spring-forward gap times (`InvalidMeetingTimeException`).
- **Conflict Detection Engine**: Half-open interval `[start, end)` checking using indexed PostgreSQL range queries (`idx_booking_occurrences_room_start`, `idx_booking_occurrences_room_end`).
- **All-or-Nothing Atomic Series Creation**: If any occurrence in a 50-occurrence series conflicts, the transaction rolls back completely and reports exact conflicting dates and times.
- **Series Editing**: Flexible scope modifications (`THIS`, `THIS_AND_FUTURE`, `WHOLE_SERIES`) with clean series splitting.
- **Redis Caching**: Caches room metadata with 10-minute TTL and automated eviction on write (`@CacheEvict`). Degrades gracefully to DB if Redis is offline.
- **Spring Actuator Monitoring**: Operational visibility at `/actuator/health`, `/actuator/metrics`, and `/actuator/prometheus`.
- **OpenAPI / Swagger UI**: Built-in interactive documentation at `/swagger-ui.html`.

---

## Core Invariant & Mathematical Conflict Rule

For any room $R$, no two active (`CONFIRMED`) booking occurrences $B_1$ and $B_2$ can overlap:

$$\text{Overlap}(B_1, B_2) \iff (B_1.\text{startTime} < B_2.\text{endTime}) \land (B_1.\text{endTime} > B_2.\text{startTime})$$

Adjacency is explicitly permitted: a meeting ending at `11:00 UTC` does not conflict with a meeting starting at `11:00 UTC`.

---

## Tech Stack

| Layer                        | Technology                     |
| ---------------------------- | ------------------------------ |
| **Language**                 | Java 21                        |
| **Framework**                | Spring Boot 3.4.3              |
| **Security**                 | Spring Security 6 + JJWT       |
| **Database**                 | PostgreSQL 17                  |
| **ORM / Persistence**        | Spring Data JPA / Hibernate    |
| **Database Migrations**      | Flyway                         |
| **Caching**                  | Redis 7 + Spring Data Redis    |
| **Monitoring**               | Spring Boot Actuator           |
| **API Documentation**        | Springdoc OpenAPI (Swagger UI) |
| **Build & Containerization** | Maven, Docker, Docker Compose  |

---

## Database ER Diagram Summary

```mermaid
erDiagram
    users ||--o{ meetings : organizes
    users ||--o{ meeting_attendees : attends
    meetings ||--o{ meeting_attendees : includes
    meetings ||--o| booking_series : defines
    meetings ||--o{ booking_occurrences : materializes
    booking_series ||--o{ booking_occurrences : generates
    rooms ||--o{ booking_occurrences : hosts
```

For full DDL schema and foreign keys, see [ER_DIAGRAM.md](./ER_DIAGRAM.md).

---

## Complexity Analysis

Let $k$ be the number of occurrences generated for a series (bounded by 12 months) and $n$ be the total number of existing bookings in the database for the room.

- **Occurrence Expansion**: $O(k)$ time complexity.
- **Conflict Query**: $O(k \log n)$ time using B-tree composite index `(room_id, start_time, end_time)`.
- **Space Complexity**: $O(k)$ persisted rows in `booking_occurrences`.

---

## Quick Start (Running via Docker Compose)

### 1. Clone & Build

```bash
docker compose up -d --build
```

### 2. Verify Services

- **Spring Boot API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator Health**: http://localhost:8080/actuator/health

For a step-by-step walkthrough of how each feature works, see the [STARTUP_AND_DEMO_GUIDE.md](./STARTUP_AND_DEMO_GUIDE.md).

---

## Demo Credentials

After `docker compose up`, log in as the seeded admin to create/manage rooms:

- email: `admin@roombooker.local`
- password: `Admin@123`

Three rooms (Falcon, Orion, Zenith) are pre-seeded so bookings can be created immediately without any manual setup.

---

## API Summary

| Method   | Endpoint                                  | Description                                             |
| -------- | ----------------------------------------- | ------------------------------------------------------- |
| `POST`   | `/api/v1/auth/register`                   | Register user & get JWT token                           |
| `POST`   | `/api/v1/auth/login`                      | Authenticate & get JWT token                            |
| `GET`    | `/api/v1/rooms`                           | List all meeting rooms (Redis Cached)                   |
| `POST`   | `/api/v1/rooms`                           | Create meeting room (ADMIN)                             |
| `POST`   | `/api/v1/bookings`                        | Create one-off room booking                             |
| `POST`   | `/api/v1/bookings/recurring`              | Create recurring booking series                         |
| `GET`    | `/api/v1/bookings/occurrences?roomId=...` | Search occurrences by date range                        |
| `PATCH`  | `/api/v1/bookings/series/{seriesId}`      | Edit series (`THIS`, `THIS_AND_FUTURE`, `WHOLE_SERIES`) |
| `DELETE` | `/api/v1/bookings/occurrences/{id}`       | Cancel single occurrence                                |
| `DELETE` | `/api/v1/bookings/series/{seriesId}`      | Cancel entire recurring series                          |

For full details, see [API.md](./API.md).

---

## Assumptions & Documented Edge-Case Decisions

- **Monthly recurrence on a day that doesn't exist in a given month** (e.g. the 31st, or a "5th Wednesday"): that month's occurrence is **skipped**, not shifted to the nearest valid day. This avoids surprising the organiser with a meeting on a date they didn't ask for.
- **DST spring-forward gap** (a local time that doesn't exist, e.g. 2:30 AM on the US "spring forward" day): the request is **rejected** with `InvalidMeetingTimeException` rather than silently shifted forward, so the organiser is told explicitly instead of getting a meeting at an unexpected time.
- **DST fall-back overlap** (a local time that happens twice): the system resolves to the **earlier** of the two valid UTC offsets, and this is a fixed, documented default rather than configurable per-request in this version.
- **Recurrence horizon** is capped at `roombooker.recurrence.max-horizon-months` (default 12, per `application.yml`) to bound how many rows a single series can materialize.
- **Attendee IDs that don't correspond to a real user** are silently skipped during booking creation rather than failing the whole request — an organiser can add attendees who may not be registered yet without the booking itself failing.

---

## Failure Handling

- All application exceptions funnel through `GlobalExceptionHandler`, which maps domain exceptions to specific HTTP status codes and a consistent JSON error shape (`ApiErrorResponse`) rather than leaking stack traces.
- Recurring series creation is fully transactional (`@Transactional` in `BookingService.createRecurringBooking`): if any generated occurrence conflicts, the exception is a `RuntimeException` subtype, so Spring rolls back the whole transaction — no partially-created series is ever left in the database.
- Redis is used only for room-metadata caching (`RoomService`, `@Cacheable`/`@CacheEvict`), never for booking correctness, so a Redis outage degrades room lookups back to PostgreSQL rather than breaking booking creation.
- **Known limitation (documented):** conflict checking is enforced at the application layer via an indexed range query (`BookingOccurrenceRepository.findConflicts`) inside a transaction, but there is **no PostgreSQL-level exclusion constraint** (`EXCLUDE USING GIST`) as a final safety net against a true concurrent double-booking race. Application-level checking inside a transaction closes the window to the duration of one request, but is not a hard database guarantee under concurrent writers.

---

## Testing

> Run `docker compose up -d postgres redis` before `./mvnw test` — the context test and Flyway migration need a live database connection.

Run unit & integration test suite:

```bash
./mvnw test
```

Tests cover:

- Daily, Weekly (multi-day), Monthly date & Nth-weekday recurrence.
- DST spring-forward gap detection and fall-back overlap resolution.
- Half-open interval conflict detection $[start, end)$.
