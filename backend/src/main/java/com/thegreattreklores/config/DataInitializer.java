package com.thegreattreklores.config;

import com.thegreattreklores.chat.entity.ChatMessage;
import com.thegreattreklores.chat.entity.ChatParticipant;
import com.thegreattreklores.chat.entity.ChatRoom;
import com.thegreattreklores.chat.enums.MessageType;
import com.thegreattreklores.chat.enums.ParticipantRole;
import com.thegreattreklores.chat.enums.ParticipantStatus;
import com.thegreattreklores.chat.enums.RoomStatus;
import com.thegreattreklores.chat.enums.RoomType;
import com.thegreattreklores.chat.repository.ChatMessageRepository;
import com.thegreattreklores.chat.repository.ChatParticipantRepository;
import com.thegreattreklores.chat.repository.ChatRoomRepository;
import com.thegreattreklores.connection.entity.GroupOrganizerConnection;
import com.thegreattreklores.connection.enums.ConnectionInitiator;
import com.thegreattreklores.connection.enums.ConnectionStatus;
import com.thegreattreklores.connection.repository.GroupOrganizerConnectionRepository;
import com.thegreattreklores.group.entity.GroupMembership;
import com.thegreattreklores.group.entity.TripGroup;
import com.thegreattreklores.group.enums.GroupStatus;
import com.thegreattreklores.group.enums.GroupVisibility;
import com.thegreattreklores.group.enums.MembershipRole;
import com.thegreattreklores.group.enums.MembershipStatus;
import com.thegreattreklores.group.repository.GroupMembershipRepository;
import com.thegreattreklores.group.repository.TripGroupRepository;
import com.thegreattreklores.notification.entity.Notification;
import com.thegreattreklores.notification.enums.NotificationType;
import com.thegreattreklores.notification.repository.NotificationRepository;
import com.thegreattreklores.organizer.entity.Organizer;
import com.thegreattreklores.organizer.enums.VerificationStatus;
import com.thegreattreklores.organizer.repository.OrganizerRepository;
import com.thegreattreklores.trek.entity.TrekDeparture;
import com.thegreattreklores.trek.entity.TrekListing;
import com.thegreattreklores.trek.enums.DepartureStatus;
import com.thegreattreklores.trek.enums.ListingStatus;
import com.thegreattreklores.trek.enums.TrekDifficulty;
import com.thegreattreklores.trek.repository.TrekDepartureRepository;
import com.thegreattreklores.trek.repository.TrekListingRepository;
import com.thegreattreklores.user.entity.User;
import com.thegreattreklores.user.entity.UserProfile;
import com.thegreattreklores.user.entity.UserSocialLink;
import com.thegreattreklores.user.entity.UserTrekHistory;
import com.thegreattreklores.user.enums.AccountStatus;
import com.thegreattreklores.user.enums.ExperienceLevel;
import com.thegreattreklores.user.enums.SocialPlatform;
import com.thegreattreklores.user.enums.UserRole;
import com.thegreattreklores.user.enums.VisibilityLevel;
import com.thegreattreklores.user.repository.UserProfileRepository;
import com.thegreattreklores.user.repository.UserRepository;
import com.thegreattreklores.user.repository.UserSocialLinkRepository;
import com.thegreattreklores.user.repository.UserTrekHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSocialLinkRepository userSocialLinkRepository;
    private final UserTrekHistoryRepository userTrekHistoryRepository;
    private final OrganizerRepository organizerRepository;
    private final TrekListingRepository trekListingRepository;
    private final TrekDepartureRepository trekDepartureRepository;
    private final TripGroupRepository tripGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final GroupOrganizerConnectionRepository connectionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmail("trekker@demo.com").isPresent()) {
            log.info("Demo database already initialized. Skipping seeding.");
            return;
        }

        log.info("Seeding demo database for The Great Trek Lores platform...");

        String encodedPassword = passwordEncoder.encode("password123");

        // 1. Users
        User trekker = userRepository.save(User.builder()
                .email("trekker@demo.com")
                .passwordHash(encodedPassword)
                .role(UserRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build());

        User companion = userRepository.save(User.builder()
                .email("companion@demo.com")
                .passwordHash(encodedPassword)
                .role(UserRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build());

        User organizerUser = userRepository.save(User.builder()
                .email("organizer@demo.com")
                .passwordHash(encodedPassword)
                .role(UserRole.ORGANIZER)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build());

        User alpineUser = userRepository.save(User.builder()
                .email("alpine@demo.com")
                .passwordHash(encodedPassword)
                .role(UserRole.ORGANIZER)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build());

        User adminUser = userRepository.save(User.builder()
                .email("admin@demo.com")
                .passwordHash(encodedPassword)
                .role(UserRole.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build());

        // 2. Profiles
        userProfileRepository.save(UserProfile.builder()
                .user(trekker)
                .displayName("Aarav Sharma")
                .profilePhotoUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80")
                .location("Bengaluru, Karnataka")
                .occupation("Software Engineer")
                .bio("Solo trekker passionate about high-altitude Himalayan summits and astrophotography.")
                .trekkingExperienceLevel(ExperienceLevel.ADVANCED)
                .build());

        userProfileRepository.save(UserProfile.builder()
                .user(companion)
                .displayName("Priya Patel")
                .profilePhotoUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                .location("Mumbai, Maharashtra")
                .occupation("Product Designer")
                .bio("Weekend trail explorer looking for friendly companion groups to tackle winter treks.")
                .trekkingExperienceLevel(ExperienceLevel.INTERMEDIATE)
                .build());

        userProfileRepository.save(UserProfile.builder()
                .user(organizerUser)
                .displayName("Vikram Negi (Himalayan Escapes)")
                .profilePhotoUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80")
                .location("Dehradun, Uttarakhand")
                .occupation("Chief Mountain Guide")
                .bio("12+ years certified mountaineering guide across Garhwal & Himachal Pradesh.")
                .trekkingExperienceLevel(ExperienceLevel.EXPERT)
                .build());

        userProfileRepository.save(UserProfile.builder()
                .user(alpineUser)
                .displayName("Tenzing Dorje (Alpine Nomad)")
                .profilePhotoUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                .location("Manali, Himachal Pradesh")
                .occupation("Expedition Leader")
                .bio("High-altitude pass crossing specialist. Guided over 80 expeditions.")
                .trekkingExperienceLevel(ExperienceLevel.EXPERT)
                .build());

        userProfileRepository.save(UserProfile.builder()
                .user(adminUser)
                .displayName("Safety Admin")
                .profilePhotoUrl("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=400&q=80")
                .location("New Delhi, India")
                .occupation("Community Safety Officer")
                .bio("Monitoring trekker safety, verifying organizers, and resolving platform reports.")
                .trekkingExperienceLevel(ExperienceLevel.EXPERT)
                .build());

        // 3. Social links & History
        userSocialLinkRepository.save(UserSocialLink.builder()
                .user(trekker)
                .platform(SocialPlatform.INSTAGRAM)
                .url("https://instagram.com/aarav_peaks")
                .visibility(VisibilityLevel.PUBLIC)
                .build());

        userSocialLinkRepository.save(UserSocialLink.builder()
                .user(companion)
                .platform(SocialPlatform.INSTAGRAM)
                .url("https://instagram.com/priyapatel_trek")
                .visibility(VisibilityLevel.GROUP_MEMBERS)
                .build());

        userTrekHistoryRepository.save(UserTrekHistory.builder()
                .user(trekker)
                .trekName("Kedarkantha Winter Peak")
                .location("Sankri, Uttarakhand")
                .startDate(LocalDate.of(2024, 12, 10))
                .endDate(LocalDate.of(2024, 12, 15))
                .description("Summited in knee-deep snow at sunrise. 12,500 ft elevation.")
                .build());

        userTrekHistoryRepository.save(UserTrekHistory.builder()
                .user(trekker)
                .trekName("Roopkund Lake Expedition")
                .location("Lohajung, Uttarakhand")
                .startDate(LocalDate.of(2023, 9, 15))
                .endDate(LocalDate.of(2023, 9, 23))
                .description("High altitude mystery lake at 15,750 ft.")
                .build());

        userTrekHistoryRepository.save(UserTrekHistory.builder()
                .user(companion)
                .trekName("Triund Ridge Trek")
                .location("Dharamshala, Himachal Pradesh")
                .startDate(LocalDate.of(2024, 5, 1))
                .endDate(LocalDate.of(2024, 5, 3))
                .description("Beautiful ridge camp under the Dhauladhar peaks.")
                .build());

        // 4. Organizers
        Organizer himalayanEscapes = organizerRepository.save(Organizer.builder()
                .ownerUser(organizerUser)
                .organizerName("Himalayan Escapes & Trails")
                .description("Premier certified trekking agency with 12+ years of guiding through Garhwal and Himachal trails. Certified wilderness first-aid guides, 4-season tents, and high-altitude gear included.")
                .location("Dehradun, Uttarakhand")
                .websiteUrl("https://himalayanescapes.example.com")
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());

        Organizer alpineNomad = organizerRepository.save(Organizer.builder()
                .ownerUser(alpineUser)
                .organizerName("Alpine Nomad Expeditions")
                .description("Boutique expedition team specializing in Hampta Pass, Chandratal, and Pin Parvati crossover treks. Small intimate batches with locally sourced organic food.")
                .location("Manali, Himachal Pradesh")
                .websiteUrl("https://alpinenomad.example.com")
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());

        // 5. Trek Listings & Departures
        TrekListing kedarkantha = trekListingRepository.save(TrekListing.builder()
                .organizer(himalayanEscapes)
                .title("Kedarkantha Winter Summit")
                .destination("Sankri / Uttarakhand")
                .description("Experience one of India's most celebrated winter summit treks. Traverse pine forests blanketed in pristine snow, camp under star-studded skies at Juda Ka Talab, and push for a glorious sunrise summit at 12,500 feet.")
                .difficulty(TrekDifficulty.MODERATE)
                .durationDays(5)
                .startingPrice(new BigDecimal("8500.00"))
                .listingStatus(ListingStatus.PUBLISHED)
                .build());

        TrekListing hamptaPass = trekListingRepository.save(TrekListing.builder()
                .organizer(alpineNomad)
                .title("Hampta Pass & Chandratal Crossover")
                .destination("Manali / Himachal Pradesh")
                .description("A breathtaking dramatic shift from the lush green valleys of Kullu to the stark, arid moonscape of Spiti. Culminates at the jewel of Himachal, the crescent-shaped Chandratal lake.")
                .difficulty(TrekDifficulty.MODERATE)
                .durationDays(5)
                .startingPrice(new BigDecimal("10500.00"))
                .listingStatus(ListingStatus.PUBLISHED)
                .build());

        TrekListing roopkund = trekListingRepository.save(TrekListing.builder()
                .organizer(himalayanEscapes)
                .title("Roopkund Mystery Lake Expedition")
                .destination("Lohajung / Uttarakhand")
                .description("High-altitude classic trek through Ali and Bedni Bugyal, India's grandest alpine meadows, reaching the snow-bound Roopkund tarn surrounded by the Trishul and Nanda Ghunti peaks.")
                .difficulty(TrekDifficulty.DIFFICULT)
                .durationDays(7)
                .startingPrice(new BigDecimal("14500.00"))
                .listingStatus(ListingStatus.PUBLISHED)
                .build());

        LocalDate now = LocalDate.now();

        // Departures
        TrekDeparture depKedarkantha1 = trekDepartureRepository.save(TrekDeparture.builder()
                .trekListing(kedarkantha)
                .startDate(now.plusDays(7))
                .endDate(now.plusDays(12))
                .price(new BigDecimal("8500.00"))
                .capacity(15)
                .availableSlots(11)
                .departureStatus(DepartureStatus.OPEN)
                .build());

        trekDepartureRepository.save(TrekDeparture.builder()
                .trekListing(kedarkantha)
                .startDate(now.plusDays(21))
                .endDate(now.plusDays(26))
                .price(new BigDecimal("8900.00"))
                .capacity(15)
                .availableSlots(14)
                .departureStatus(DepartureStatus.OPEN)
                .build());

        TrekDeparture depHampta1 = trekDepartureRepository.save(TrekDeparture.builder()
                .trekListing(hamptaPass)
                .startDate(now.plusDays(14))
                .endDate(now.plusDays(19))
                .price(new BigDecimal("10500.00"))
                .capacity(12)
                .availableSlots(8)
                .departureStatus(DepartureStatus.OPEN)
                .build());

        trekDepartureRepository.save(TrekDeparture.builder()
                .trekListing(roopkund)
                .startDate(now.plusDays(30))
                .endDate(now.plusDays(37))
                .price(new BigDecimal("14500.00"))
                .capacity(10)
                .availableSlots(7)
                .departureStatus(DepartureStatus.OPEN)
                .build());

        // 6. Trip Groups
        TripGroup group1 = tripGroupRepository.save(TripGroup.builder()
                .createdByUser(trekker)
                .name("Kedarkantha Solo Trekkers Cohort")
                .destination("Sankri / Uttarakhand")
                .description("Connecting solo trekkers wanting to ascend Kedarkantha next week. We are pooling our group to negotiate the best local organizer package with top safety gear.")
                .preferredStartDate(now.plusDays(7))
                .preferredEndDate(now.plusDays(12))
                .datesFlexible(true)
                .maxMembers(6)
                .status(GroupStatus.OPEN)
                .visibility(GroupVisibility.PUBLIC)
                .build());

        groupMembershipRepository.save(GroupMembership.builder()
                .group(group1)
                .user(trekker)
                .role(MembershipRole.ADMIN)
                .status(MembershipStatus.ACCEPTED)
                .build());

        groupMembershipRepository.save(GroupMembership.builder()
                .group(group1)
                .user(companion)
                .role(MembershipRole.MEMBER)
                .status(MembershipStatus.ACCEPTED)
                .build());

        TripGroup group2 = tripGroupRepository.save(TripGroup.builder()
                .createdByUser(companion)
                .name("Hampta Pass Weekend Explorers")
                .destination("Manali / Himachal Pradesh")
                .description("Looking for 3-4 more nature enthusiasts to cross Hampta Pass and camp beside the crystal waters of Chandratal.")
                .preferredStartDate(now.plusDays(14))
                .preferredEndDate(now.plusDays(19))
                .datesFlexible(false)
                .maxMembers(5)
                .status(GroupStatus.OPEN)
                .visibility(GroupVisibility.PUBLIC)
                .build());

        groupMembershipRepository.save(GroupMembership.builder()
                .group(group2)
                .user(companion)
                .role(MembershipRole.ADMIN)
                .status(MembershipStatus.ACCEPTED)
                .build());

        // 7. Group-Organizer Connection (Group 1 with Himalayan Escapes)
        GroupOrganizerConnection connection = connectionRepository.save(GroupOrganizerConnection.builder()
                .group(group1)
                .organizer(himalayanEscapes)
                .initiatedBy(ConnectionInitiator.GROUP)
                .message("Hello Vikram! Our group of solo trekkers would love to enquire about joining your Kedarkantha departure on " + depKedarkantha1.getStartDate() + ".")
                .status(ConnectionStatus.ACCEPTED)
                .build());

        // 8. Dual-Room Chats
        // Room A: MAIN_GROUP (Trekkers only)
        ChatRoom groupChatRoom = chatRoomRepository.save(ChatRoom.builder()
                .group(group1)
                .roomType(RoomType.MAIN_GROUP)
                .status(RoomStatus.ACTIVE)
                .createdByUser(trekker)
                .build());

        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(groupChatRoom)
                .user(trekker)
                .role(ParticipantRole.GROUP_ADMIN)
                .status(ParticipantStatus.ACTIVE)
                .build());

        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(groupChatRoom)
                .user(companion)
                .role(ParticipantRole.MEMBER)
                .status(ParticipantStatus.ACTIVE)
                .build());

        chatMessageRepository.save(ChatMessage.builder()
                .chatRoom(groupChatRoom)
                .senderUser(trekker)
                .messageType(MessageType.TEXT)
                .messageText("Hey Priya! Welcome to the Kedarkantha crew. Are you bringing microspikes or planning to rent from basecamp?")
                .build());

        chatMessageRepository.save(ChatMessage.builder()
                .chatRoom(groupChatRoom)
                .senderUser(companion)
                .messageType(MessageType.TEXT)
                .messageText("Hey Aarav! I'll rent them at Sankri. I've sent an inquiry to Himalayan Escapes since their batch matches our dates.")
                .build());

        // Room B: ORGANIZER_DISCUSSION (Trekkers + Organizer)
        ChatRoom orgDiscussionRoom = chatRoomRepository.save(ChatRoom.builder()
                .group(group1)
                .connection(connection)
                .roomType(RoomType.ORGANIZER_DISCUSSION)
                .status(RoomStatus.ACTIVE)
                .createdByUser(trekker)
                .build());

        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(orgDiscussionRoom)
                .user(trekker)
                .role(ParticipantRole.GROUP_ADMIN)
                .status(ParticipantStatus.ACTIVE)
                .build());

        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(orgDiscussionRoom)
                .user(companion)
                .role(ParticipantRole.MEMBER)
                .status(ParticipantStatus.ACTIVE)
                .build());

        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(orgDiscussionRoom)
                .user(organizerUser)
                .role(ParticipantRole.ORGANIZER)
                .status(ParticipantStatus.ACTIVE)
                .build());

        chatMessageRepository.save(ChatMessage.builder()
                .chatRoom(orgDiscussionRoom)
                .senderUser(organizerUser)
                .messageType(MessageType.TEXT)
                .messageText("Hello Aarav and Priya! Glad to connect. We've accepted your group inquiry. We have 4-season tents, certified mountain guides, and full medical kits reserved for your departure.")
                .build());

        chatMessageRepository.save(ChatMessage.builder()
                .chatRoom(orgDiscussionRoom)
                .senderUser(trekker)
                .messageType(MessageType.TEXT)
                .messageText("Thanks Vikram! What time is the pickup from Dehradun railway station on " + depKedarkantha1.getStartDate() + "?")
                .build());

        chatMessageRepository.save(ChatMessage.builder()
                .chatRoom(orgDiscussionRoom)
                .senderUser(organizerUser)
                .messageType(MessageType.TEXT)
                .messageText("Pickup is at 6:30 AM outside Dehradun station by our private tempo traveler. We reach Sankri basecamp by 4:00 PM.")
                .build());

        // 9. Notifications
        notificationRepository.save(Notification.builder()
                .recipientUser(trekker)
                .notificationType(NotificationType.CONNECTION_ACCEPTED)
                .title("Connection Accepted")
                .body("Himalayan Escapes & Trails has accepted your group enquiry. Organizer discussion room is now open!")
                .relatedEntityType("CONNECTION")
                .relatedEntityId(connection.getId())
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .recipientUser(organizerUser)
                .notificationType(NotificationType.CONNECTION_ENQUIRY)
                .title("New Group Inquiry")
                .body("Kedarkantha Solo Trekkers Cohort has enquired about your upcoming departure.")
                .relatedEntityType("CONNECTION")
                .relatedEntityId(connection.getId())
                .isRead(true)
                .build());

        log.info("Demo database successfully initialized with users, organizers, treks, departures, groups, chats, and notifications.");
    }
}
