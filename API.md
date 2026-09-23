# API Documentation — RoomBooker

## Base URL
`http://localhost:8080/api/v1`

---

## Authentication Endpoints

### 1. Register User
`POST /api/v1/auth/register`

**Request Body:**
```json
{
  "name": "Abhilash Reddy",
  "email": "abhilash@example.com",
  "password": "Password123"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "c1f2e3d4-...",
    "name": "Abhilash Reddy",
    "email": "abhilash@example.com",
    "role": "USER"
  }
}
```

---

## Room Endpoints

### 2. Create Room (ADMIN)
`POST /api/v1/rooms`

**Header:** `Authorization: Bearer <ADMIN_JWT>`

**Request Body:**
```json
{
  "name": "Boardroom A",
  "capacity": 15,
  "location": "Building 3, Floor 2"
}
```

---

## Booking Endpoints

### 3. Create One-Off Booking
`POST /api/v1/bookings`

**Header:** `Authorization: Bearer <JWT>`

**Request Body:**
```json
{
  "roomId": "f3b2a1c0-...",
  "title": "Architecture Review",
  "startTime": "2026-10-05T10:00:00Z",
  "endTime": "2026-10-05T11:00:00Z",
  "attendeeUserIds": []
}
```

---

### 4. Create Recurring Booking Series
`POST /api/v1/bookings/recurring`

**Header:** `Authorization: Bearer <JWT>`

**Request Body:**
```json
{
  "roomId": "f3b2a1c0-...",
  "title": "Weekly Backend Sync",
  "startDate": "2026-10-05",
  "endDate": "2026-12-28",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "timezone": "Asia/Kolkata",
  "recurrenceType": "WEEKLY",
  "intervalValue": 1,
  "weekdays": "MONDAY,WEDNESDAY"
}
```

**Conflict Error Response (409 Conflict):**
```json
{
  "code": "BOOKING_CONFLICT",
  "message": "Recurring booking conflicts with existing meetings",
  "timestamp": "2026-09-22T17:30:00Z",
  "conflicts": [
    {
      "date": "2026-10-14",
      "requestedStart": "2026-10-14T04:30:00Z",
      "requestedEnd": "2026-10-14T05:30:00Z",
      "existingBookingId": "a9b8c7d6-...",
      "existingMeetingTitle": "Quarterly All Hands",
      "existingStart": "2026-10-14T04:00:00Z",
      "existingEnd": "2026-10-14T05:00:00Z"
    }
  ]
}
```
