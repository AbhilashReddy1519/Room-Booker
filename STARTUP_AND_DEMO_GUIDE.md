# RoomBooker — Complete Startup & End-to-End Testing Manual

This guide provides step-by-step instructions to start the RoomBooker service, understand all system capabilities, and run every single feature test using `curl` commands.

---

## 1. How to Start & Run the Project

### Prerequisites
- **Docker & Docker Compose** installed and running.
- Alternatively: Java 21, Maven 3.9+, PostgreSQL 17, Redis 7 running locally.

### Startup Command
Run the containerized application via Docker Compose:
```bash
docker compose up -d --build
```

### Verification
Once containers start, check service statuses:
- **Spring Boot App**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Application Logs**:
  ```bash
  docker compose logs -f app
  ```

---

## 2. Step-by-Step Feature Testing Walkthrough

Follow these steps in order to test every capability in RoomBooker.

### Step 1: Admin Login & Obtain Admin Token
Log in using the pre-seeded admin user credentials:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@roombooker.local",
    "password": "Admin@123"
  }'
```
**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Admin User",
    "email": "admin@roombooker.local",
    "role": "ADMIN"
  }
}
```
> **Save the token**: Copy the `token` string for subsequent authenticated requests as `ADMIN_TOKEN`.

---

### Step 2: List Seeded Rooms (Redis Cached)
List all available rooms:
```bash
curl -X GET http://localhost:8080/api/v1/rooms \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```
**Expected Response (200 OK):**
Returns 3 pre-seeded rooms: `Falcon`, `Orion`, and `Zenith`.
```json
[
  {
    "id": "22222222-2222-2222-2222-222222222221",
    "name": "Falcon",
    "capacity": 10,
    "location": "Block A, 2nd Floor",
    "createdAt": "2026-09-23T11:21:43Z"
  },
  ...
]
```
> **Save Room ID**: Note `22222222-2222-2222-2222-222222222221` (Falcon) as `ROOM_ID`.

---

### Step 3: Register a Regular User
Register a standard non-admin user:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Smith",
    "email": "alice@example.com",
    "password": "UserPass123"
  }'
```
**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "a91b2c3d-...",
    "name": "Alice Smith",
    "email": "alice@example.com",
    "role": "USER"
  }
}
```
> **Save User Token**: Copy the returned JWT token as `USER_TOKEN`.

---

### Step 4: Test Role-Based Access Control (RBAC) on Room Creation
1. **Try creating a room as regular user (Alice)**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <USER_TOKEN>" \
     -d '{
       "name": "Titan",
       "capacity": 30,
       "location": "Block C, 1st Floor"
     }'
   ```
   **Expected Response:** `403 Forbidden` (`Access Denied`).

2. **Create room as Admin**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <ADMIN_TOKEN>" \
     -d '{
       "name": "Titan",
       "capacity": 30,
       "location": "Block C, 1st Floor"
     }'
   ```
   **Expected Response (201 Created):** Returns new room `Titan`.

---

### Step 5: Create a One-Off Booking
Book room `Falcon` for `2026-10-05` from `10:00:00Z` to `11:00:00Z`:
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -d '{
    "roomId": "22222222-2222-2222-2222-222222222221",
    "title": "Q4 Strategy Meeting",
    "startTime": "2026-10-05T10:00:00Z",
    "endTime": "2026-10-05T11:00:00Z",
    "attendeeUserIds": []
  }'
```
**Expected Response (201 Created):** Returns `MeetingResponse` with 1 confirmed occurrence.

---

### Step 6: Test Overlap Conflict Detection (One-Off)
Attempt to book an overlapping time slot (`10:30Z` to `11:30Z`) for the same room:
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -d '{
    "roomId": "22222222-2222-2222-2222-222222222221",
    "title": "Design Sync",
    "startTime": "2026-10-05T10:30:00Z",
    "endTime": "2026-10-05T11:30:00Z",
    "attendeeUserIds": []
  }'
```
**Expected Response (409 Conflict):**
```json
{
  "code": "BOOKING_CONFLICT",
  "message": "Room is unavailable for requested time slot",
  "timestamp": "...",
  "conflicts": [
    {
      "date": "2026-10-05",
      "requestedStart": "2026-10-05T10:30:00Z",
      "requestedEnd": "2026-10-05T11:30:00Z",
      "existingBookingId": "...",
      "existingMeetingTitle": "Q4 Strategy Meeting",
      "existingStart": "2026-10-05T10:00:00Z",
      "existingEnd": "2026-10-05T11:00:00Z"
    }
  ]
}
```

---

### Step 7: Create a Recurring Booking Series
Create a weekly recurring meeting every Monday & Wednesday for 1 month:
```bash
curl -X POST http://localhost:8080/api/v1/bookings/recurring \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -d '{
    "roomId": "22222222-2222-2222-2222-222222222221",
    "title": "Weekly Team Standup",
    "startDate": "2026-10-12",
    "endDate": "2026-11-09",
    "startTime": "14:00:00",
    "endTime": "15:00:00",
    "timezone": "Asia/Kolkata",
    "recurrenceType": "WEEKLY",
    "intervalValue": 1,
    "weekdays": "MONDAY,WEDNESDAY"
  }'
```
**Expected Response (201 Created):** Returns series details along with generated occurrences. Save `series.id` as `SERIES_ID`.

---

### Step 8: Test All-or-Nothing Atomic Rollback on Series Creation
Create a recurring series that overlaps on **just one** date with an existing meeting:
```bash
curl -X POST http://localhost:8080/api/v1/bookings/recurring \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -d '{
    "roomId": "22222222-2222-2222-2222-222222222221",
    "title": "Conflicting Series Test",
    "startDate": "2026-10-05",
    "endDate": "2026-10-26",
    "startTime": "10:15:00",
    "endTime": "10:45:00",
    "timezone": "UTC",
    "recurrenceType": "WEEKLY",
    "intervalValue": 1,
    "weekdays": "MONDAY"
  }'
```
**Expected Response (409 Conflict):**
- System detects clash on `2026-10-05`.
- Transaction rolls back completely (`@Transactional` in `BookingService`).
- **Zero** new occurrences or meetings are inserted into PostgreSQL.

---

### Step 9: Search Occurrences by Room and Date Range
Retrieve all occurrences in `Falcon` for October 2026:
```bash
curl -X GET "http://localhost:8080/api/v1/bookings/occurrences?roomId=22222222-2222-2222-2222-222222222221&from=2026-10-01T00:00:00Z&to=2026-10-31T23:59:59Z" \
  -H "Authorization: Bearer <USER_TOKEN>"
```
**Expected Response (200 OK):** Returns array of all confirmed occurrences in October.

---

### Step 10: Edit Series Scope `THIS` (Single Occurrence Update)
Update room or time for just one single occurrence in a series:
```bash
curl -X PATCH http://localhost:8080/api/v1/bookings/series/<SERIES_ID> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -d '{
    "scope": "THIS",
    "targetOccurrenceDate": "2026-10-14",
    "startTime": "15:00:00",
    "endTime": "16:00:00"
  }'
```
**Expected Response (200 OK):** Only the occurrence on `2026-10-14` is updated; remaining series occurrences remain unchanged.

---

### Step 11: Edit Series Scope `THIS_AND_FUTURE` (Split Series)
Split series starting from `2026-10-21` onwards:
```bash
curl -X PATCH http://localhost:8080/api/v1/bookings/series/<SERIES_ID> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -d '{
    "scope": "THIS_AND_FUTURE",
    "targetOccurrenceDate": "2026-10-21",
    "startTime": "16:00:00",
    "endTime": "17:00:00"
  }'
```
**Expected Response (200 OK):** Old series is truncated at `2026-10-20`, and a new series is instantiated starting `2026-10-21`.

---

### Step 12: Cancel Single Occurrence
Cancel one specific occurrence by ID:
```bash
curl -X DELETE http://localhost:8080/api/v1/bookings/occurrences/<OCCURRENCE_ID> \
  -H "Authorization: Bearer <USER_TOKEN>"
```
**Expected Response (204 No Content):** Status of target occurrence updated to `CANCELLED`.

---

### Step 13: Cancel Entire Series
Cancel all occurrences in a recurring series:
```bash
curl -X DELETE http://localhost:8080/api/v1/bookings/series/<SERIES_ID> \
  -H "Authorization: Bearer <USER_TOKEN>"
```
**Expected Response (204 No Content):** All occurrences linked to `<SERIES_ID>` updated to `CANCELLED`.

---

### Step 14: Test Redis Graceful Degradation
Verify that stopping Redis does not break room listing:
1. Stop Redis container:
   ```bash
   docker compose stop redis
   ```
2. Request room list again:
   ```bash
   curl -X GET http://localhost:8080/api/v1/rooms \
     -H "Authorization: Bearer <USER_TOKEN>"
   ```
   **Expected Response:** HTTP 200 OK (served directly from PostgreSQL).
3. Check application logs (`docker compose logs app`):
   ```
   WARN [com.roombooker.cache] Redis GET failed for cache='rooms' key='SimpleKey []' — falling back to DB: ...
   ```
4. Restart Redis container:
   ```bash
   docker compose start redis
   ```

---

### Step 15: Verify Spring Actuator Monitoring
Check application metrics and health endpoints:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics/http.server.requests
curl http://localhost:8080/actuator/prometheus
```
