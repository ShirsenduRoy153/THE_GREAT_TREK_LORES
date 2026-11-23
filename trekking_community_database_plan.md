# Trekking Community Platform — Database Plan

A lightweight database outline for the portfolio MVP. This document lists table names and suggested columns only. It separates **KEEP for MVP**, **OPTIONAL later**, and **SKIP for now** so the first version stays manageable.

## Conventions

- Primary keys: `id` (UUID or BIGINT; choose one strategy and use it consistently).
- Foreign keys: columns ending in `_id`.
- Timestamps: `created_at`, `updated_at` where useful.
- Passwords must be stored as secure password hashes, never plain text.
- Image files should live in Cloudinary or object storage. Store only their URLs/storage keys and metadata in PostgreSQL.
- Use enums or checked values for status/type columns.

---

# 1. KEEP — MVP tables

## 1. `users`
Login account for every person using the platform, including regular users, organizer representatives and admins.

- `id`
- `email`
- `password_hash`
- `role` — `USER`, `ORGANIZER`, `ADMIN`
- `account_status` — `ACTIVE`, `SUSPENDED`, `DEACTIVATED`
- `email_verified`
- `created_at`
- `updated_at`

## 2. `user_profiles`
Public profile information for a regular user.

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

## 3. `user_social_links`
Optional social/contact links. Each link has its own visibility setting.

- `id`
- `user_id`
- `platform` — `INSTAGRAM`, `WHATSAPP`, `FACEBOOK`, `LINKEDIN`, `SPOTIFY`, `OTHER`
- `url`
- `visibility` — `PRIVATE`, `GROUP_MEMBERS`, `PUBLIC`
- `created_at`
- `updated_at`

## 4. `user_trek_history`
Completed treks and achievements shown on a user's profile.

- `id`
- `user_id`
- `trek_name`
- `location`
- `start_date`
- `end_date`
- `description`
- `created_at`

## 5. `trip_groups`
A temporary group created by a user looking for trekking companions.

- `id`
- `created_by_user_id`
- `name`
- `destination`
- `description`
- `preferred_start_date`
- `preferred_end_date`
- `dates_flexible`
- `max_members`
- `visibility` — `PUBLIC`, `PRIVATE`
- `status` — `OPEN`, `PLANNING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`
- `created_at`
- `updated_at`

## 6. `group_memberships`
Membership and join-request status for each user in a group.

- `id`
- `group_id`
- `user_id`
- `role` — `ADMIN`, `MEMBER`
- `status` — `PENDING`, `ACCEPTED`, `REJECTED`, `LEFT`, `REMOVED`
- `joined_at`
- `created_at`
- `updated_at`

## 7. `organizers`
Organizer/company profile. An organizer account is linked to a user account.

- `id`
- `owner_user_id`
- `organizer_name`
- `description`
- `logo_url`
- `location`
- `website_url`
- `verification_status` — `PENDING`, `VERIFIED`, `REJECTED`
- `created_at`
- `updated_at`

## 8. `trek_listings`
A trek/package offered by an organizer, such as Pangarchulla or Kedarkantha.

- `id`
- `organizer_id`
- `title`
- `destination`
- `description`
- `difficulty`
- `duration_days`
- `starting_price`
- `cover_image_url`
- `listing_status` — `DRAFT`, `PUBLISHED`, `INACTIVE`
- `created_at`
- `updated_at`

## 9. `trek_departures`
Specific scheduled departures under a trek listing. One listing can have many departures.

- `id`
- `trek_listing_id`
- `start_date`
- `end_date`
- `price`
- `capacity`
- `available_slots`
- `departure_status` — `OPEN`, `FULL`, `CANCELLED`, `COMPLETED`
- `created_at`
- `updated_at`

## 10. `group_organizer_connections`
A private connection/enquiry between one trekking group and one organizer.

- `id`
- `group_id`
- `organizer_id`
- `initiated_by` — `GROUP`, `ORGANIZER`
- `message`
- `status` — `PENDING`, `ACCEPTED`, `REJECTED`, `CONFIRMED`, `CLOSED`
- `created_at`
- `updated_at`

**Important:** `CONFIRMED` is just a status on this connection. It does not create another chat room, and a group can confirm more than one organizer if it wants.

## 11. `chat_rooms`
One main private group room, plus separate rooms for each group–organizer connection.

- `id`
- `group_id`
- `group_organizer_connection_id` — nullable; null for the main group chat
- `room_type` — `MAIN_GROUP`, `ORGANIZER_DISCUSSION`
- `status` — `ACTIVE`, `CLOSED`, `ARCHIVED`
- `created_by_user_id`
- `created_at`
- `updated_at`

Suggested rule: one `MAIN_GROUP` room per trip group, and one `ORGANIZER_DISCUSSION` room per accepted group–organizer connection.

## 12. `chat_participants`
Controls who can access each chat room.

- `id`
- `chat_room_id`
- `user_id` — nullable if using an organizer-level participant identity
- `organizer_id` — nullable when participant is a regular user
- `participant_role` — `GROUP_ADMIN`, `MEMBER`, `ORGANIZER`
- `joined_at`
- `left_at`
- `participant_status` — `ACTIVE`, `REMOVED`, `LEFT`

Implementation note: the simplest approach is to give every organizer representative a normal `users` account and use `user_id` for all participants. If you do that, omit `organizer_id` from this table and identify the organizer through the connection.

## 13. `chat_messages`
Text and image messages sent in either type of chat room.

- `id`
- `chat_room_id`
- `sender_user_id`
- `message_type` — `TEXT`, `IMAGE`, `FILE`
- `message_text` — nullable for image/file-only messages
- `media_url` — nullable
- `media_file_name` — nullable
- `created_at`
- `edited_at`
- `deleted_at`

## 14. `notifications`
In-app alerts for join requests, approvals and organizer connection activity.

- `id`
- `recipient_user_id`
- `notification_type`
- `title`
- `body`
- `related_entity_type`
- `related_entity_id`
- `is_read`
- `created_at`

## 15. `reports`
Reports submitted by users about profiles, groups, organizers or content.

- `id`
- `reported_by_user_id`
- `target_type`
- `target_id`
- `reason`
- `details`
- `status` — `OPEN`, `UNDER_REVIEW`, `RESOLVED`, `REJECTED`
- `reviewed_by_admin_user_id`
- `created_at`
- `updated_at`

## 16. `admin_actions`
An audit trail of important admin actions.

- `id`
- `admin_user_id`
- `action_type`
- `target_type`
- `target_id`
- `reason`
- `created_at`

---

# 2. OPTIONAL — Add when the core platform works

These support the social-network features, but can be added after groups, organizer connections and chat are working.

## 17. `user_posts`
A user's feed post or trekking story.

- `id`
- `user_id`
- `caption`
- `visibility` — `PUBLIC`, `GROUPS`, `PRIVATE`
- `created_at`
- `updated_at`

## 18. `post_media`
Images/videos attached to a post. A separate table allows multiple images per post.

- `id`
- `post_id`
- `media_url`
- `media_type` — `IMAGE`, `VIDEO`
- `display_order`
- `created_at`

## 19. `user_stories`
Short-lived profile stories. Only build this if you specifically want stories that expire.

- `id`
- `user_id`
- `media_url`
- `caption`
- `expires_at`
- `created_at`

## 20. `user_gallery`
Extra trekking photos shown on a user's profile, separate from feed posts.

- `id`
- `user_id`
- `media_url`
- `caption`
- `trek_name`
- `created_at`

---

# 3. SKIP for the first version

Avoid these until there is a real need:

- Separate `confirmed_trip_chats` table — confirmation is a status on `group_organizer_connections`; keep using the existing organizer discussion room.
- Separate organizer-created social groups — organizers publish `trek_listings` and `trek_departures`; users create trekking companion groups.
- Separate group photo table — use chat image messages or user posts for the MVP.
- Payments, bookings, refunds and invoices — not part of the initial bridge-between-trekkers-and-organizers scope.
- Reviews and ratings — can be added after real trips happen.
- Likes, comments, follows and friend requests — optional social features, not needed to validate the main idea.
- Redis, Kafka and microservices — unnecessary for a small portfolio MVP.

---

# 4. A few database rules to remember

1. Add a unique constraint on `users.email`.
2. Add a unique constraint on (`group_id`, `user_id`) in `group_memberships`.
3. Prevent duplicate active main rooms for the same group.
4. Prevent duplicate organizer discussion rooms for the same group–organizer connection.
5. Check chat-room membership on the backend before returning messages or accepting a new message.
6. Do not trust the frontend to enforce privacy. Enforce room access and social-link visibility in Spring Boot.
7. Store image files outside PostgreSQL; store only their URLs or storage keys in the database.
8. Add indexes to frequently searched fields such as group destination/date, trek destination, departure dates, and chat message (`chat_room_id`, `created_at`).

## Suggested MVP scope

Start with tables 1–16. Add posts and media tables 17–18 when you are ready to build the social feed. Add expiring stories and a separate gallery only if those features are still needed.

The table count is a design starting point, not a requirement to implement everything at once.
