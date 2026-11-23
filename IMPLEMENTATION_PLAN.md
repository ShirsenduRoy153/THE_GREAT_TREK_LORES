# Implementation Plan & Progress Tracker

> **Project:** The Great Trek Lores (Trekking Community Platform)  
> **Target:** 100% Zero-Cost, Permanent Demo Portfolio MVP  
> **Last Updated:** 2026-11-24  
> **Current Status:** All 7 Phases Complete — 100% Zero-Cost Portfolio Ready  

---

## 1. Phase Progress Dashboard

| Phase | Description | Status | Progress | Notes |
| :--- | :--- | :---: | :---: | :--- |
| **Phase 1** | Foundation & Zero-Cost Scaffolding | ✅ Done | 100% | Backend (Java 17/Spring Boot), Frontend (React/Vite/Tailwind), Docker Compose, Health Check |
| **Phase 2** | Auth, Roles & Profile Management | ✅ Done | 100% | Tables 1–4, Spring Security + JWT filter, Profile CRUD with privacy, One-Click Demo Logins |
| **Phase 3** | Trip Groups & Trek Departures | ✅ Done | 100% | Tables 5–9, Companion groups, scheduled departures, date overlap matcher |
| **Phase 4** | Group–Organizer Matchmaking | ✅ Done | 100% | Table 10, Bidirectional connections, status flow (Pending, Accepted, Confirmed) |
| **Phase 5** | Dual-Room Chat & Real-Time WebSocket | ✅ Done | 100% | Tables 11–13, Isolated rooms, WebSocket STOMP, Dual-tab chat UI |
| **Phase 6** | Cloudinary Media & Moderation | ✅ Done | 100% | Tables 14–16, Media pipeline, notifications, reporting and admin moderation |
| **Phase 7** | Zero-Cost Deployment & Demo Polish | ✅ Done | 100% | Render, Neon, Vercel, 512MB JVM tuning, Pre-seeded DB, Portfolio docs |

---

## 2. Detailed Phase Breakdown & Tasks

### Phase 1: Foundation & Zero-Cost Scaffolding
- [x] Create project documentation (`README.md`, `IMPLEMENTATION_PLAN.md`, `API_SPECIFICATION.md`, `ARCHITECTURE_AND_SECURITY.md`).
- [x] Initialize repository structure (`/backend` and `/frontend`).
- [x] Setup Spring Boot backend with Maven (Temurin Java 17 LTS):
  - [x] Dependencies configured: `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`, `spring-boot-starter-websocket`, `postgresql`, `lombok`, `springdoc-openapi-starter-webmvc-ui`, `jjwt`.
  - [x] Configure `application.yml` with profiles (`local`, `prod`).
  - [x] Configure Swagger / OpenAPI UI at `/swagger-ui.html`.
  - [x] Add `/api/health` endpoint returning system status and profile.
- [x] Setup React frontend with Vite + TypeScript:
  - [x] Configure Tailwind CSS with custom palette.
  - [x] Set up Axios API client base with JWT interceptors.
  - [x] Health check status indicator integrated on landing page.
- [x] Setup Docker Compose for local PostgreSQL (`docker-compose.yml`, port 5432).
- [x] Environment template configured in `.env.example` with Neon.tech connection guidance.

---

### Phase 2: Authentication, Roles & Profile Management
- [x] **Database Setup (Tables 1–4):**
  - [x] `users`: email, password hash, role (`USER`, `ORGANIZER`, `ADMIN`), status.
  - [x] `user_profiles`: display name, photo URL, bio, trek level.
  - [x] `user_social_links`: platform, url, visibility (`PRIVATE`, `GROUP_MEMBERS`, `PUBLIC`).
  - [x] `user_trek_history`: trek name, location, dates, achievements.
- [x] **Backend Auth & Security:**
  - [x] Spring Security configuration with `JwtAuthenticationFilter`.
  - [x] Endpoints: `/api/auth/register`, `/api/auth/login`, `/api/auth/me`.
  - [x] BCrypt password hashing.
  - [x] Profile CRUD endpoints (`/api/users/me/profile`, `/api/users/{userId}/profile`).
  - [x] Social links visibility enforcement on the backend.
- [x] **Frontend Auth UI:**
  - [x] Register & Login modal with input validation and role selector.
  - [x] Instant 1-Click Demo Logins for recruiters (`Trekker`, `Organizer`, `Admin`).
  - [x] JWT storage and Axios request interceptor.
  - [x] User Profile view and edit modal with field-level privacy toggles and trek history logger.

---

### Phase 3: Trip Groups & Organizer Trek Listings
- [x] **Database Setup (Tables 5–9):**
  - [x] `trip_groups`: destination, preferred dates, flexibility, status.
  - [x] `group_memberships`: group_id, user_id, role (`ADMIN`, `MEMBER`), status (`PENDING`, `ACCEPTED`, `REJECTED`).
  - [x] `organizers`: company name, logo, location, verification status.
  - [x] `trek_listings`: title, destination, difficulty, base price.
  - [x] `trek_departures`: listing_id, start/end dates, price, capacity, slots.
- [x] **Backend Workflows:**
  - [x] Trip Groups: Create, browse, search by destination & dates, update status.
  - [x] Join Request Flow: Submit join request, Group Admin review & approval/rejection.
  - [x] Organizer Listings: Create organizer profile, publish trek packages.
  - [x] Scheduled Departures: Add departure date batches with capacity limits & pricing.
  - [x] Date Overlap Matcher Query: Filter treks and companion groups whose dates coincide.
- [x] **Frontend Group & Trek UI:**
  - [x] Discovery Feed with tabs: Companion Groups vs Trek Packages.
  - [x] Destination filter search bar.
  - [x] "Create Group" modal with validation and date flexibility toggle.
  - [x] Group Details modal with confirmed companion roster, join button, and admin approvals.
  - [x] Trek Package details with scheduled departure batch breakdown.
  - [x] Live "Matching Organizers" tab inside group details highlighting overlapping departures.

---

### Phase 4: Group–Organizer Matchmaking
- [x] **Database Setup (Table 10):**
  - [x] `group_organizer_connections`: group_id, organizer_id, initiated_by, status (`PENDING`, `ACCEPTED`, `REJECTED`, `CONFIRMED`, `CLOSED`).
- [x] **Backend Connection Logic:**
  - [x] Group-to-Organizer enquiry endpoint (`POST /api/connections/groups/{groupId}/enquire/{organizerId}`).
  - [x] Organizer-to-Group pitch endpoint (`POST /api/connections/organizers/{organizerId}/pitch/{groupId}`).
  - [x] State transition controls (Group admin & organizer verification for accept/confirm/reject).
  - [x] Confirming an organizer updates connection status to `CONFIRMED` without requiring payment gateways.
- [x] **Frontend Matchmaking UI:**
  - [x] "Enquire with Organizer" button on matching departure cards.
  - [x] Dedicated "Connections" tab inside Group Details showing current connection status and "Confirm Organizer" button.
  - [x] Organizer Inquiries & Connections inbox modal for organizers to review incoming enquiries and accept/reject pitches.

---

### Phase 5: Dual-Room Chat & Real-Time Messaging
- [x] **Database Setup (Tables 11–13):**
  - [x] `chat_rooms`: group_id, connection_id (nullable), room_type (`MAIN_GROUP`, `ORGANIZER_DISCUSSION`).
  - [x] `chat_participants`: room_id, user_id, role, status.
  - [x] `chat_messages`: room_id, sender_id, message_type, text, media_url.
- [x] **Backend Chat System:**
  - [x] Automatic room provisioning:
    - Auto-create `MAIN_GROUP` room when a trip group is created (trekkers only; organizers blocked).
    - Auto-create `ORGANIZER_DISCUSSION` room when a connection is `ACCEPTED`.
  - [x] Backend room authorization guard (enforced on every message read and write).
  - [x] REST endpoints: room list, message history, and fallback send.
  - [x] Spring WebSocket + STOMP integration (`/ws-chat` endpoint, `/topic/chat/rooms/{roomId}` broadcast).
- [x] **Frontend Chat UI:**
  - [x] Multi-room chat modal with room switcher (Companion Chat vs Organizer Discussion).
  - [x] Message thread stream with sender display names, roles (`GROUP_ADMIN`, `MEMBER`, `ORGANIZER`), and timestamps.
  - [x] Integrated "Open Group Chat" action inside Group Details modal.
  - [x] Auto-polling live sync for zero-spin-down reliability on free-tier hosting.

---

### Phase 6: Cloudinary Media Integration & Moderation
- [x] **Database Setup (Tables 14–16):**
  - [x] `notifications`: in-app alerts for join requests and connection updates.
  - [x] `reports`: user-reported profiles, groups, or messages.
  - [x] `admin_actions`: audit trail for admin interventions.
- [x] **Cloudinary Integration:**
  - [x] Cloudinary backend SDK integration with local/zero-cost demo fallback in `MediaService.java`.
  - [x] Image upload endpoint (`/api/media/upload`, `/api/media/upload-trekker-photo`) with file type and size validation.
  - [x] Save only generated URLs in PostgreSQL.
- [x] **Notifications & Moderation:**
  - [x] In-app notification delivery for join requests, approvals, and connection changes.
  - [x] Reporting dialog (`ReportModal.tsx`) on groups, organizers, and user profiles with `POST /api/reports`.
  - [x] Basic Admin moderation endpoints (`/api/admin/reports`, `/api/admin/reports/{id}/resolve`, `/api/admin/reports/{id}/dismiss`) to flag content and record audit actions.
  - [x] Frontend `NotificationDropdown.tsx` with live unread badge, mark read, and polling.

---

### Phase 7: Zero-Cost Deployment & Demo Polish
- [x] **Zero-Cost Deployment Optimization:**
  - [x] Multi-stage `Dockerfile` with memory optimization (`-XX:+UseSerialGC -Xmx256m -Xss512k -XX:MaxMetaspaceSize=128m`) for 512MB RAM free hosts.
  - [x] Infrastructure as Code Render Blueprint (`render.yaml`) connecting backend to serverless **Neon** PostgreSQL.
  - [x] Frontend SPA deployment configuration (`vercel.json`) with security headers.
  - [x] Production CORS and Hikari connection pool limits (max 5 connections) tuned for Neon serverless tier.
- [x] **Demo Polish (Portfolio-Ready):**
  - [x] Automated Database Seeder (`DataInitializer.java`):
    - Verified organizers (*Himalayan Escapes & Trails*, *Alpine Nomad Expeditions*) with full itineraries and departure batches.
    - Pre-seeded trip groups (*Kedarkantha Solo Trekkers Cohort*, *Hampta Pass Weekend Explorers*) with date-matched departures.
    - Dual-room chats seeded with realistic dialogues (Main Trekker group chat vs Organizer discussion room).
    - Pre-seeded in-app notifications.
    - Test accounts (`trekker@demo.com`, `companion@demo.com`, `organizer@demo.com`, `admin@demo.com` / password: `password123`).
  - [x] One-Click Demo Logins for instant recruiter testing (`Trekker`, `Organizer`, `Admin`).
  - [x] Comprehensive documentation update across `README.md`, `IMPLEMENTATION_PLAN.md`, `API_SPECIFICATION.md`, and `ARCHITECTURE_AND_SECURITY.md`.

---

## 3. Maintenance and Documentation Rule

> [!IMPORTANT]
> **Living Document Policy:**  
> As implementation proceeds, this document must be updated at each milestone:
> 1. Check off completed items `[x]`.
> 2. Update phase completion percentages and status badges in Section 1.
> 3. Document any architectural adjustments or schema modifications directly in the notes.
