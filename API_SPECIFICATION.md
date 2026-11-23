# API & WebSocket Specification

> **Base URL:** `/api`  
> **Auth Header:** `Authorization: Bearer <JWT_TOKEN>`  
> **Version:** 1.0 (MVP)

---

## 1. Authentication & Profiles Module (`auth` & `user`)

### `POST /api/auth/register`
Register a new user account.
* **Access:** Public
* **Request Body:**
```json
{
  "email": "trekker@example.com",
  "password": "SecurePassword123!",
  "role": "USER",
  "displayName": "Alex Rivers"
}
```
* **Response (201 Created):**
```json
{
  "userId": "uuid-here",
  "email": "trekker@example.com",
  "role": "USER",
  "token": "jwt.token.string"
}
```

### `POST /api/auth/login`
Authenticate existing user.
* **Access:** Public
* **Request Body:**
```json
{
  "email": "trekker@example.com",
  "password": "SecurePassword123!"
}
```
* **Response (200 OK):**
```json
{
  "token": "jwt.token.string",
  "userId": "uuid-here",
  "role": "USER",
  "displayName": "Alex Rivers"
}
```

### `GET /api/users/{userId}/profile`
Fetch public profile of a trekker.
* **Access:** Authenticated
* **Response (200 OK):**
```json
{
  "userId": "uuid-here",
  "displayName": "Alex Rivers",
  "profilePhotoUrl": "https://res.cloudinary.com/.../avatar.jpg",
  "location": "Bengaluru, India",
  "occupation": "Software Engineer",
  "bio": "Avid Himalayan trekker.",
  "trekkingExperienceLevel": "INTERMEDIATE",
  "socialLinks": [
    { "platform": "INSTAGRAM", "url": "https://instagram.com/alex" },
    { "platform": "WHATSAPP", "url": "+919876543210" }
  ],
  "trekHistory": [
    { "trekName": "Hampta Pass", "startDate": "2024-06-12", "endDate": "2024-06-17" }
  ]
}
```
*(Note: WhatsApp link is only included if requester is an approved group companion of this user).*

### `PUT /api/users/me/profile`
Update own profile details.
* **Access:** Authenticated (Self)

---

## 2. Trip Groups Module (`group`)

### `POST /api/groups`
Create a new companion trip group.
* **Access:** Authenticated (`USER`)
* **Request Body:**
```json
{
  "name": "Kedarkantha Winter Summit 2026",
  "destination": "Kedarkantha",
  "description": "Looking for 4 more passionate trekkers to share tents and guide costs.",
  "preferredStartDate": "2026-12-20",
  "preferredEndDate": "2026-12-25",
  "datesFlexible": true,
  "maxMembers": 6
}
```
* **Response (201 Created):** Group created; creator automatically added as `ADMIN`; `MAIN_GROUP` chat room provisioned automatically.

### `GET /api/groups`
Search and filter trip groups.
* **Access:** Public / Authenticated
* **Query Params:** `destination`, `startDate`, `endDate`, `status` (`OPEN`), `page`, `size`.

### `POST /api/groups/{groupId}/join-requests`
Submit a request to join a group.
* **Access:** Authenticated (`USER`)
* **Response (201 Created):** Creates membership with status `PENDING`.

### `PATCH /api/groups/{groupId}/memberships/{membershipId}`
Approve or reject a member request.
* **Access:** Group Admin only
* **Request Body:**
```json
{
  "status": "ACCEPTED"
}
```
*(Accepted members automatically gain read/write access to the group's `MAIN_GROUP` chat).*

---

## 3. Organizers & Treks Module (`organizer` & `trek`)

### `POST /api/organizers`
Create an organizer/agency profile.
* **Access:** Authenticated (`ORGANIZER`)
* **Request Body:**
```json
{
  "organizerName": "Himalayan Highs",
  "description": "Certified Mountaineering Guides operating in Uttarakhand & Himachal.",
  "location": "Dehradun, India",
  "websiteUrl": "https://himalayanhighs.com"
}
```

### `POST /api/organizers/{organizerId}/treks`
Publish a new trek listing.
* **Access:** Organizer Owner
* **Request Body:**
```json
{
  "title": "Kedarkantha Peak Trek",
  "destination": "Kedarkantha",
  "difficulty": "EASY_MODERATE",
  "durationDays": 6,
  "startingPrice": 8500.00,
  "coverImageUrl": "https://res.cloudinary.com/.../kedarkantha.jpg"
}
```

### `POST /api/treks/{trekListingId}/departures`
Add a scheduled departure batch under a trek listing.
* **Access:** Organizer Owner
* **Request Body:**
```json
{
  "startDate": "2026-12-18",
  "endDate": "2026-12-24",
  "price": 8500.00,
  "capacity": 15,
  "availableSlots": 15
}
```

### `GET /api/treks/matching-departures`
Find departures that overlap with a group's dates.
* **Query Params:** `groupId`
* **Filter Rule:** `group_start_date <= departure_end_date AND group_end_date >= departure_start_date`

---

## 4. Group–Organizer Connections Module (`connection`)

### `POST /api/groups/{groupId}/connections`
Group Admin initiates connection enquiry to an Organizer.
* **Access:** Group Admin

### `POST /api/organizers/{organizerId}/connections`
Organizer pitches package to an open Trip Group.
* **Access:** Organizer Owner

### `PATCH /api/connections/{connectionId}/status`
Update connection state.
* **Request Body:**
```json
{
  "status": "ACCEPTED"
}
```
* **State Transitions:**
  - `PENDING` $\rightarrow$ `ACCEPTED` (Auto-creates an `ORGANIZER_DISCUSSION` chat room).
  - `PENDING` $\rightarrow$ `REJECTED`.
  - `ACCEPTED` $\rightarrow$ `CONFIRMED` (Marks group planning finalized with this organizer; keeps same chat room).
  - Any $\rightarrow$ `CLOSED`.

---

## 5. Chat & WebSocket Module (`chat`)

### REST Endpoints

#### `GET /api/chat/rooms`
List all accessible chat rooms for current user (Main group chats + Organizer discussion rooms).
* **Access:** Authenticated

#### `GET /api/chat/rooms/{roomId}/messages`
Fetch paginated message history.
* **Access:** Authorized Room Participants only
* **Query Params:** `page`, `size` (default: 30)

---

### WebSocket (STOMP over WebSocket)

* **Connection Endpoint:** `/ws-chat` (Supports SockJS fallback)
* **STOMP Broker Prefixes:**
  - Inbound application messages: `/app`
  - Outbound message subscriptions: `/topic`
  - User-specific queues: `/user/queue`

#### 1. Subscribe to Room Messages
* **Destination:** `/topic/chat/rooms/{roomId}`
* **Payload received:**
```json
{
  "id": "message-uuid",
  "roomId": "room-uuid",
  "senderUserId": "user-uuid",
  "senderName": "Alex Rivers",
  "messageType": "TEXT",
  "messageText": "What time are we meeting at Dehradun station?",
  "mediaUrl": null,
  "createdAt": "2026-11-22T10:30:00Z"
}
```

#### 2. Send Message
* **Destination:** `/app/chat.sendMessage`
* **Payload sent:**
```json
{
  "roomId": "room-uuid",
  "messageType": "TEXT",
  "messageText": "What time are we meeting at Dehradun station?",
  "mediaUrl": null
}
```
* **Security Validation:** Backend intercepts message, verifies sender is an active participant in `roomId`, writes to PostgreSQL, and broadcasts to `/topic/chat/rooms/{roomId}`.

---

## 6. Media & Moderation Module (`media` & `admin`)

### `POST /api/media/upload`
Upload an image to Cloudinary via backend proxy.
* **Access:** Authenticated
* **Content-Type:** `multipart/form-data`
* **Form Field:** `file` (Max 5MB; image/jpeg, image/png, image/webp)
* **Response (200 OK):**
```json
{
  "url": "https://res.cloudinary.com/demo/image/upload/v12345/sample.jpg"
}
```

### `POST /api/reports`
Submit an abuse or spam report.
* **Request Body:**
```json
{
  "targetType": "USER",
  "targetId": "uuid-here",
  "reason": "Harassment in chat",
  "details": "User was sharing unsolicited promotional links."
}
```

### `GET /api/notifications`
Fetch all in-app notifications for the authenticated user.
* **Access:** Authenticated

### `PATCH /api/notifications/{id}/read`
Mark a single notification as read.
* **Access:** Authenticated (Recipient only)

### `PATCH /api/notifications/read-all`
Mark all notifications for the authenticated user as read.
* **Access:** Authenticated

### `GET /api/admin/reports`
List open reports for platform moderation.
* **Access:** `ADMIN` role only.

### `PATCH /api/admin/reports/{id}/resolve`
Resolve a reported issue and record admin action.
* **Access:** `ADMIN` role only.

### `PATCH /api/admin/reports/{id}/dismiss`
Dismiss a false report.
* **Access:** `ADMIN` role only.
