# Architecture, Security & Zero-Cost Optimization Guide

This document specifies the technical design, security rules, and performance tunings required to keep the **Trekking Community Platform** secure and functional on permanent free tiers.

---

## 1. System Architecture: Modular Monolith

To eliminate multi-container network overhead and fit inside free 512MB RAM compute instances, the backend is strictly a **Modular Monolith** in a single Spring Boot application:

```
[ frontend/ (React SPA on Vercel) ]
             │ REST (HTTPS) & WebSocket (WSS)
             ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot Modular Monolith               │
│                                                         │
│  [ auth ]        [ user ]        [ group ]              │
│  [ organizer ]   [ trek ]        [ connection ]         │
│  [ chat ]        [ media ]       [ notification/admin ] │
│                                                         │
│        Spring Security + JPA / Hibernate                │
└─────────────────────────────────────────────────────────┘
             │
             ▼ SSL Connection Pooling (HikariCP)
[ Neon.tech Serverless PostgreSQL ]
```

### Package Structure
```
com.thegreattreklores
├── auth/            # JWT, SecurityConfig, AuthController
├── user/            # User profile, social links, trek history
├── group/           # Trip groups, memberships, approval logic
├── organizer/       # Organizer entity, verification
├── trek/            # Trek listings & departure schedules
├── connection/      # Group-Organizer matchmaking & states
├── chat/            # Dual-room logic, WebSocket STOMP handlers
├── media/           # Cloudinary service, file validation
├── common/          # GlobalExceptionHandler, BaseEntity, Auditing
└── config/          # WebMvcConfig, WebSocketConfig, SwaggerConfig
```

---

## 2. Dual-Room Chat Isolation & Privacy Architecture

The platform enforces strict physical separation between group companion chatter and commercial organizer negotiations.

```
                  ┌──────────────────────┐
                  │ Trip Group Created   │
                  └──────────┬───────────┘
                             │ (Automatic)
                             ▼
                  ┌──────────────────────┐
                  │ Room: MAIN_GROUP     │
                  │ Members: Trekkers    │
                  │ (Organizers BLOCKED) │
                  └──────────────────────┘

  ┌────────────────────────────────────────────────────────┐
  │ Group Admin & Organizer A Accept Connection            │
  └──────────────────────────┬─────────────────────────────┘
                             │ (Automatic)
                             ▼
                  ┌──────────────────────┐
                  │ Room: ORGANIZER_DISC │
                  │ Members: Group +     │
                  │          Organizer A │
                  │ (Organizer B BLOCKED)│
                  └──────────────────────┘
```

### Backend Authorization Rules
1. **Never trust frontend parameters:** A user cannot post or read messages simply by sending a `roomId`. The backend executes a lookup in `chat_participants`:
   ```sql
   SELECT COUNT(1) FROM chat_participants 
   WHERE chat_room_id = :roomId 
     AND user_id = :userId 
     AND participant_status = 'ACTIVE';
   ```
2. **Organizer Exclusion:** In `MAIN_GROUP` rooms, any user with `role = ORGANIZER` is prohibited from being added as a participant.
3. **Multi-Organizer Isolation:** If a group discusses with both Organizer A and Organizer B, two distinct `ORGANIZER_DISCUSSION` rooms exist. Organizer A cannot see Organizer B's room or messages.
4. **Member Removal Cascade:** When a member leaves or is removed from a `trip_group`, their status in `chat_participants` is immediately set to `REMOVED`.

---

## 3. Contact & Social Link Visibility Security

To prevent unsolicited contact and preserve privacy:
- Trekkers can add sensitive contact info (e.g., WhatsApp, phone numbers).
- Each link has a visibility level: `PUBLIC`, `GROUP_MEMBERS`, or `PRIVATE`.
- **Visibility Resolution Logic:**
  ```java
  if (link.getVisibility() == Visibility.PUBLIC) {
      return link.getUrl();
  } else if (link.getVisibility() == Visibility.GROUP_MEMBERS) {
      boolean areGroupCompanions = groupMembershipRepository.shareApprovedGroup(viewerId, targetUserId);
      return areGroupCompanions ? link.getUrl() : null;
  } else {
      return viewerId.equals(targetUserId) ? link.getUrl() : null;
  }
  ```

---

## 4. Zero-Cost Performance & 512MB RAM Tuning

Free-tier cloud containers (such as Render or Koyeb) enforce a hard limit of **512 MB RAM**. If the JVM exceeds this, the container is killed (`OOMKilled`).

### 1. JVM Flags for Containerization
Include these flags in the production Dockerfile entrypoint:
```dockerfile
ENTRYPOINT ["java", \
  "-XX:+UseSerialGC", \
  "-Xmx256m", \
  "-Xss512k", \
  "-XX:TieredStopAtLevel=1", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```
* **`-XX:+UseSerialGC`**: Avoids the high memory overhead of G1GC for small single-core heaps.
* **`-Xmx256m`**: Limits maximum heap to 256MB, leaving ~250MB for Metaspace, threads, and container OS.
* **`-Xss512k`**: Reduces thread stack size from the 1MB default, saving substantial RAM under multiple threads.
* **`-XX:TieredStopAtLevel=1`**: Disables C2 compiler profiling, drastically reducing JVM warm-up memory.

### 2. Embedded Tomcat & Connection Pool Tuning
In `application-prod.yml`:
```yaml
server:
  tomcat:
    threads:
      max: 30         # Default is 200; 30 easily handles demo traffic without RAM spikes
      min-spare: 5
    connection-timeout: 10000

spring:
  datasource:
    hikari:
      maximum-pool-size: 5  # Neon free tier limits connections; keep pool modest
      minimum-idle: 2
      idle-timeout: 30000
      max-lifetime: 1800000
```

---

## 5. Media Pipeline & Storage Safety

- **No Images in PostgreSQL:** Never store binary blobs (`bytea`) in the database.
- **Direct/Proxy Upload to Cloudinary:** Backend validates file extensions (`.jpg`, `.jpeg`, `.png`, `.webp`) and MIME types, ensuring files do not exceed **5MB**.
- **Cloudinary Transformations:** Request compressed thumbnails on the frontend to save bandwidth:
  ```
  https://res.cloudinary.com/<cloud>/image/upload/w_400,c_fill,q_auto,f_auto/<asset_id>
  ```
