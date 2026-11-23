# Trekking Community Platform — Project Plan

## 1. Project overview

Build a lightweight, trekking-focused social website that helps people find trekking companions and gives small trekking organizers a place to present their profiles, treks, schedules and contact options.

The platform's main purpose is to bridge the initial communication gap between trekkers and organizers. Users can discover groups and organizers, discuss plans privately on the platform, and move to WhatsApp or another channel themselves once they are comfortable.

This is a portfolio/MVP project. Keep the architecture simple and suitable for low-cost or free hosting.

## 2. Main user roles

### 2.1 Regular user / trekker

A regular user can:

- Register, log in and manage a profile.
- Add a profile photo, location, occupation and short bio.
- Add optional social/contact links with privacy controls.
- Show completed treks, experience and achievements.
- Upload trekking photos and share posts/stories when those features are enabled.
- Create a temporary trip group for a trek and an approximate date range.
- Browse and search other trip groups.
- Request to join a group.
- Participate in the group's private main chat after acceptance.
- Discover organizers and start or respond to organizer discussions.
- Share text and images in permitted chat rooms.

### 2.2 Organizer

An organizer can be an individual trek leader or a trekking company.

An organizer can:

- Create and manage an organizer profile.
- Upload a logo, profile images and trek photos.
- Publish trek listings/packages.
- Add multiple scheduled departures under each listing.
- Browse groups that may match their trek and departure dates.
- Send a connection request to a group admin.
- Accept enquiries from groups.
- Participate in a separate private discussion room for each connected group.
- Manage several group conversations from one organizer dashboard.

An organizer account should be linked to a login account. If a company later needs multiple staff members, additional organizer-team membership can be added as a future feature.

### 2.3 Platform admin

Admins can:

- Review and manage users and organizers.
- Verify or reject organizer profiles.
- Review reports.
- Suspend accounts or moderate inappropriate content.
- View an audit history of important administrative actions.

## 3. Main product concepts

Keep these concepts separate:

| Concept | Created by | What it means |
|---|---|---|
| Trekking group | Regular user | A temporary group of people looking for companions for a particular trek |
| Trek listing | Organizer | A trek/package offered by the organizer |
| Trek departure | Organizer | A specific scheduled date range for a trek listing |
| Organizer connection | Group or organizer | A request/enquiry linking one group and one organizer |
| Main group chat | Automatically when group is created | Private discussion among accepted group members |
| Organizer discussion room | After a connection is accepted | Private conversation between one group and one organizer |

## 4. Trip group workflow

1. A user creates a group with a destination, approximate start/end dates, date flexibility and description.
2. The creator becomes the group admin.
3. A main group chat is created automatically.
4. Other users discover the group and request to join.
5. The group admin accepts or rejects requests.
6. Accepted members can access the main group chat.
7. The group admin can update trip details and manage membership.
8. The group can remain open, become confirmed/planning, and eventually be completed or cancelled.

Suggested group statuses:

- `OPEN`
- `PLANNING`
- `CONFIRMED`
- `COMPLETED`
- `CANCELLED`

A group is not an organizer's tour roster. It is a user-created companion group.

## 5. Organizer and trek workflow

### 5.1 Organizer profile

One organizer account can publish multiple trek listings.

Example:

- Organizer: Mountain Trails
  - Listing: Pangarchulla
  - Listing: Kedarkantha
  - Listing: Sandakphu

### 5.2 Trek listings and departures

A listing describes the trek/package. A departure is one specific scheduled batch.

Example:

- Pangarchulla listing
  - Departure 1: 3 October–7 October
  - Departure 2: 6 October–10 October
  - Departure 3: 10 October–15 October
- Kedarkantha listing
  - Departure 1: 20 October–25 October

Each departure can have its own price, capacity, available slots and status. Do not store all departure dates as one date range on the organizer profile.

A group's preferred date range can be compared with a departure range using date overlap:

`group_start_date <= departure_end_date AND group_end_date >= departure_start_date`

This identifies possible matches; the group and organizer still discuss suitability.

## 6. Group–organizer connection workflow

A group and organizer can connect in either direction.

### Group starts the enquiry

1. Group admin discovers an organizer.
2. Group admin sends an enquiry.
3. Organizer accepts or rejects.
4. If accepted, an organizer discussion room is created.

### Organizer approaches a group

1. Organizer discovers a relevant group.
2. Organizer sends a connection request to the group admin.
3. Group admin accepts or rejects.
4. If accepted, an organizer discussion room is created.

A group may talk to multiple organizers at the same time. An organizer may talk to multiple groups at the same time.

Suggested connection statuses:

- `PENDING`
- `ACCEPTED`
- `REJECTED`
- `CONFIRMED`
- `CLOSED`

`CONFIRMED` is only a status on the group–organizer connection. It is not a booking, payment or a new chat room. A group may mark more than one organizer as confirmed if that suits its planning. The platform does not force an exclusive selection.

## 7. Chat design

There are two core chat room types.

### 7.1 Main group chat — `MAIN_GROUP`

- Created automatically when a trip group is created.
- Contains only accepted members of that trip group.
- The group admin is a participant with an admin role.
- Organizers cannot access this room.
- Members can exchange text and images.
- Membership changes must update chat access.

### 7.2 Organizer discussion room — `ORGANIZER_DISCUSSION`

- Created only after a group–organizer connection is accepted.
- One separate room for each group–organizer connection.
- Contains the relevant group members and the organizer's authorized representative/account.
- Other organizers cannot see the room or its messages.
- Used for initial questions, itinerary details, dates, prices and image sharing.
- When the group marks the organizer connection as confirmed, keep using the same room. Do not create a separate confirmed-trip chat.
- The group can have separate rooms with several organizers.
- An organizer can have separate rooms with several groups.

The platform is intended to support initial discussions. After the group and organizer are comfortable, they can exchange external contact details voluntarily and continue on WhatsApp or another service. Do not automatically expose phone numbers or transfer users off-platform.

### Chat privacy rules

- Check room membership on the backend for every read and send operation.
- Do not rely only on hiding rooms in the frontend.
- A user removed from a group must lose access to that group's private chat.
- An organizer must only access rooms associated with accepted connections.
- Do not show one organizer's conversation to another organizer.
- Keep message history when a room is closed or archived, subject to the platform's retention policy.

## 8. Profiles, posts and media

### User profile

Suggested profile information:

- Display name
- Profile picture
- Location
- Occupation
- Bio
- Trekking experience level
- Completed trek history
- Optional social links
- Optional trekking gallery

### Organizer profile

Suggested information:

- Organizer/company name
- Description
- Logo/profile image
- Location
- Website or public social links
- Verification status
- Trek listings

### Media handling

Users and organizers should be able to upload images where relevant:

- Profile photos and logos
- Trek listing photos
- User gallery and posts
- Images in chat messages

Store actual files in Cloudinary or compatible object storage. Store only the media URL/storage key and metadata in PostgreSQL. Validate file type and size on the backend.

For the first version, prioritize profile images, trek listing images and images in chat. Posts, stories and a separate gallery can be added after the core workflows work.

## 9. Suggested database structure

The following is a proposed schema outline. It is a starting point; confirm constraints and relationships while drawing the ER diagram.

### 9.1 Core MVP tables — KEEP

#### 1. `users`

- `id`
- `email`
- `password_hash`
- `role` — `USER`, `ORGANIZER`, `ADMIN`
- `account_status`
- `email_verified`
- `created_at`
- `updated_at`

#### 2. `user_profiles`

- `id`
- `user_id`
- `display_name`
- `profile_photo_url`
- `location`
- `occupation`
- `bio`
- `trekking_experience_level`
- `created_at`
- `updated_at`

#### 3. `user_social_links`

- `id`
- `user_id`
- `platform`
- `url`
- `visibility`
- `created_at`
- `updated_at`

#### 4. `user_trek_history`

- `id`
- `user_id`
- `trek_name`
- `location`
- `start_date`
- `end_date`
- `description`
- `created_at`

#### 5. `trip_groups`

- `id`
- `created_by_user_id`
- `name`
- `destination`
- `description`
- `preferred_start_date`
- `preferred_end_date`
- `dates_flexible`
- `max_members`
- `visibility`
- `status`
- `created_at`
- `updated_at`

#### 6. `group_memberships`

- `id`
- `group_id`
- `user_id`
- `role`
- `status`
- `joined_at`
- `created_at`
- `updated_at`

#### 7. `organizers`

- `id`
- `owner_user_id`
- `organizer_name`
- `description`
- `logo_url`
- `location`
- `website_url`
- `verification_status`
- `created_at`
- `updated_at`

#### 8. `trek_listings`

- `id`
- `organizer_id`
- `title`
- `destination`
- `description`
- `difficulty`
- `duration_days`
- `starting_price`
- `cover_image_url`
- `listing_status`
- `created_at`
- `updated_at`

#### 9. `trek_departures`

- `id`
- `trek_listing_id`
- `start_date`
- `end_date`
- `price`
- `capacity`
- `available_slots`
- `departure_status`
- `created_at`
- `updated_at`

#### 10. `group_organizer_connections`

- `id`
- `group_id`
- `organizer_id`
- `initiated_by`
- `message`
- `status`
- `created_at`
- `updated_at`

#### 11. `chat_rooms`

- `id`
- `group_id`
- `group_organizer_connection_id` — nullable for the main group room
- `room_type`
- `status`
- `created_by_user_id`
- `created_at`
- `updated_at`

#### 12. `chat_participants`

Recommended simple approach: every organizer representative also has a normal `users` account, so all participants use `user_id`.

- `id`
- `chat_room_id`
- `user_id`
- `participant_role`
- `joined_at`
- `left_at`
- `participant_status`

#### 13. `chat_messages`

- `id`
- `chat_room_id`
- `sender_user_id`
- `message_type` — `TEXT`, `IMAGE`, `FILE`
- `message_text`
- `media_url`
- `media_file_name`
- `created_at`
- `edited_at`
- `deleted_at`

#### 14. `notifications`

- `id`
- `recipient_user_id`
- `notification_type`
- `title`
- `body`
- `related_entity_type`
- `related_entity_id`
- `is_read`
- `created_at`

#### 15. `reports`

- `id`
- `reported_by_user_id`
- `target_type`
- `target_id`
- `reason`
- `details`
- `status`
- `reviewed_by_admin_user_id`
- `created_at`
- `updated_at`

#### 16. `admin_actions`

- `id`
- `admin_user_id`
- `action_type`
- `target_type`
- `target_id`
- `reason`
- `created_at`

### 9.2 Optional social tables — ADD LATER

#### 17. `user_posts`

- `id`
- `user_id`
- `caption`
- `visibility`
- `created_at`
- `updated_at`

#### 18. `post_media`

- `id`
- `post_id`
- `media_url`
- `media_type`
- `display_order`
- `created_at`

#### 19. `user_stories`

- `id`
- `user_id`
- `media_url`
- `caption`
- `expires_at`
- `created_at`

#### 20. `user_gallery`

- `id`
- `user_id`
- `media_url`
- `caption`
- `trek_name`
- `created_at`

### 9.3 Skip for the first version

- Separate confirmed-trip chat table: confirmation is a connection status; keep using the existing organizer discussion room.
- Organizer-created social groups: organizers publish listings/departures; users create companion groups.
- Separate group photo table: use chat image messages or posts initially.
- Booking, payment, refund and invoice tables.
- Reviews and ratings.
- Likes, comments, follows and friend requests.
- Separate chat attachments table until richer file management is needed.
- Redis, Kafka, microservices and Kubernetes for the initial deployment.

## 10. Technology stack

Recommended lightweight stack:

| Layer | Technology |
|---|---|
| Frontend | React |
| Backend | Java + Spring Boot REST APIs |
| Database | PostgreSQL |
| ORM/data access | Spring Data JPA / Hibernate |
| Authentication | Spring Security, JWT, BCrypt |
| Real-time chat | Spring WebSocket + STOMP |
| Image storage | Cloudinary or S3-compatible object storage |
| API documentation | Swagger / OpenAPI |
| Testing | JUnit 5, Mockito, Postman |
| Build | Maven |
| Deployment | Docker on a suitable low-cost/free host |
| Source control | Git + GitHub |

Use a modular monolith: one Spring Boot application divided into packages/modules. Do not start with microservices.

WebSocket is useful for real-time chat, but it can be implemented after normal REST APIs, group membership and chat-room authorization are working.

## 11. Suggested backend modules

- `auth` — registration, login, token handling
- `user` — user profile and social links
- `organizer` — organizer profile and verification
- `trek` — listings and departures
- `group` — trip groups and membership requests
- `connection` — group–organizer requests and statuses
- `chat` — rooms, participants, messages and WebSocket events
- `media` — image upload and validation
- `notification` — in-app notifications
- `admin` — moderation and audit actions

## 12. Suggested API outline

Example endpoints; exact paths can be adjusted during implementation.

### Authentication and profiles

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/{userId}`
- `PUT /api/users/me`
- `POST /api/users/me/social-links`

### Groups

- `POST /api/groups`
- `GET /api/groups`
- `GET /api/groups/{groupId}`
- `POST /api/groups/{groupId}/join-requests`
- `GET /api/groups/{groupId}/join-requests`
- `PATCH /api/groups/{groupId}/members/{membershipId}`

### Organizers and treks

- `POST /api/organizers`
- `GET /api/organizers`
- `GET /api/organizers/{organizerId}`
- `POST /api/organizers/{organizerId}/treks`
- `POST /api/treks/{trekListingId}/departures`
- `GET /api/treks`

### Group–organizer connections

- `POST /api/groups/{groupId}/organizer-connections`
- `POST /api/organizers/{organizerId}/group-connections`
- `PATCH /api/organizer-connections/{connectionId}`

### Chat

- `GET /api/chat/rooms`
- `GET /api/chat/rooms/{roomId}/messages`
- WebSocket destination for sending/receiving messages, after room authorization is implemented.

### Media and notifications

- `POST /api/media/upload`
- `GET /api/notifications`
- `PATCH /api/notifications/{notificationId}/read`

## 13. Security and privacy requirements

- Hash passwords using BCrypt or an appropriate Spring Security password encoder.
- Enforce roles and resource ownership on the backend.
- Only group admins can manage their group's join requests and settings.
- Only accepted members can access the main group chat.
- Only participants in an organizer discussion room can access it.
- Social/contact links should have explicit visibility controls. WhatsApp should not be public by default.
- Validate uploaded file type, size and ownership.
- Rate-limit sensitive actions such as login and repeated connection requests.
- Provide report/block or moderation workflows before opening the platform publicly.

## 14. Implementation roadmap

### Phase 1 — Project foundation

- Create GitHub repository.
- Set up React frontend and Spring Boot backend.
- Configure PostgreSQL and environment-specific configuration.
- Add Docker Compose for local development.
- Add Swagger and a basic health endpoint.

### Phase 2 — Authentication and profiles

- Registration and login.
- Role-based access.
- User profile and social links.
- Organizer profile.

### Phase 3 — Groups and organizers

- Create, browse and update trip groups.
- Join requests and membership management.
- Trek listings and scheduled departures.
- Search by destination and approximate date range.

### Phase 4 — Group–organizer connections

- Group-to-organizer enquiries.
- Organizer-to-group connection requests.
- Accept/reject/confirm/close statuses.
- Notifications for connection changes.

### Phase 5 — Chat

- Create main group chat automatically.
- Create organizer discussion room after connection acceptance.
- Enforce participant access.
- Add text messages first.
- Add image messages.
- Add WebSocket/STOMP for real-time updates.

### Phase 6 — Media and social features

- Profile and organizer image uploads.
- Trek listing photos.
- Optional user posts, gallery and stories.
- Add moderation/reporting improvements.

### Phase 7 — Deployment and polish

- Deploy backend and frontend.
- Configure production database and image storage.
- Add logging and error handling.
- Add tests for permissions, memberships and connection workflows.
- Write README with architecture, ER diagram, API documentation and demo instructions.

## 15. Key design decisions to preserve

1. A group has one private main chat for accepted trekkers.
2. Each accepted group–organizer connection has its own separate discussion room.
3. One group can talk to multiple organizers.
4. One organizer can talk to multiple groups.
5. Confirming an organizer updates a status; it does not create another room or booking.
6. Organizer discussions are private to that organizer and the relevant group participants.
7. Organizers publish trek listings and departures; regular users create trekking companion groups.
8. Users and organizers can upload images, but actual files are stored outside PostgreSQL.
9. Build the core workflows first. Add social-feed extras only after the main use case works.

---

**MVP target:** A user can create a profile, create or join a trekking group, chat privately with group members, discover an organizer, connect and discuss plans in a separate private room, while the organizer can manage trek listings, departure dates and multiple group conversations.
