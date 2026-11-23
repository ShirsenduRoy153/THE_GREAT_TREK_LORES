package com.thegreattreklores.chat.service;

import com.thegreattreklores.chat.dto.ChatDtos.*;
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
import com.thegreattreklores.group.entity.GroupMembership;
import com.thegreattreklores.group.entity.TripGroup;
import com.thegreattreklores.group.enums.MembershipRole;
import com.thegreattreklores.group.enums.MembershipStatus;
import com.thegreattreklores.group.repository.GroupMembershipRepository;
import com.thegreattreklores.user.entity.User;
import com.thegreattreklores.user.entity.UserProfile;
import com.thegreattreklores.user.repository.UserProfileRepository;
import com.thegreattreklores.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository roomRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatRoom provisionMainGroupRoom(TripGroup group, User creator) {
        Optional<ChatRoom> existing = roomRepository.findByGroupIdAndRoomType(group.getId(), RoomType.MAIN_GROUP);
        if (existing.isPresent()) {
            return existing.get();
        }

        ChatRoom room = ChatRoom.builder()
                .group(group)
                .roomType(RoomType.MAIN_GROUP)
                .status(RoomStatus.ACTIVE)
                .createdByUser(creator)
                .build();

        room = roomRepository.save(room);

        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(room)
                .user(creator)
                .role(ParticipantRole.GROUP_ADMIN)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        participantRepository.save(participant);
        return room;
    }

    @Transactional
    public ChatRoom provisionOrganizerDiscussionRoom(GroupOrganizerConnection connection) {
        Optional<ChatRoom> existing = roomRepository.findByConnectionId(connection.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        TripGroup group = connection.getGroup();
        User organizerUser = connection.getOrganizer().getOwnerUser();

        ChatRoom room = ChatRoom.builder()
                .group(group)
                .connection(connection)
                .roomType(RoomType.ORGANIZER_DISCUSSION)
                .status(RoomStatus.ACTIVE)
                .createdByUser(organizerUser)
                .build();

        room = roomRepository.save(room);

        // Add organizer
        ChatParticipant organizerParticipant = ChatParticipant.builder()
                .chatRoom(room)
                .user(organizerUser)
                .role(ParticipantRole.ORGANIZER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
        participantRepository.save(organizerParticipant);

        // Add all current accepted members of the group
        List<GroupMembership> members = membershipRepository.findByGroupIdAndStatus(group.getId(), MembershipStatus.ACCEPTED);
        for (GroupMembership m : members) {
            ChatParticipant p = ChatParticipant.builder()
                    .chatRoom(room)
                    .user(m.getUser())
                    .role(m.getRole() == MembershipRole.ADMIN ? ParticipantRole.GROUP_ADMIN : ParticipantRole.MEMBER)
                    .status(ParticipantStatus.ACTIVE)
                    .joinedAt(Instant.now())
                    .build();
            participantRepository.save(p);
        }

        return room;
    }

    @Transactional
    public void addMemberToGroupRooms(UUID groupId, User user, ParticipantRole role) {
        List<ChatRoom> rooms = roomRepository.findByGroupId(groupId);
        for (ChatRoom room : rooms) {
            Optional<ChatParticipant> existing = participantRepository.findByChatRoomIdAndUserId(room.getId(), user.getId());
            if (existing.isPresent()) {
                ChatParticipant p = existing.get();
                p.setStatus(ParticipantStatus.ACTIVE);
                participantRepository.save(p);
            } else {
                ChatParticipant p = ChatParticipant.builder()
                        .chatRoom(room)
                        .user(user)
                        .role(role)
                        .status(ParticipantStatus.ACTIVE)
                        .joinedAt(Instant.now())
                        .build();
                participantRepository.save(p);
            }
        }
    }

    @Transactional
    public void removeMemberFromGroupRooms(UUID groupId, UUID userId) {
        List<ChatRoom> rooms = roomRepository.findByGroupId(groupId);
        for (ChatRoom room : rooms) {
            participantRepository.findByChatRoomIdAndUserId(room.getId(), userId).ifPresent(p -> {
                p.setStatus(ParticipantStatus.REMOVED);
                p.setLeftAt(Instant.now());
                participantRepository.save(p);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDto> getUserRooms(UUID userId) {
        return roomRepository.findUserActiveRooms(userId).stream()
                .map(r -> mapToRoomDto(r, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDto> getGroupRooms(UUID groupId, UUID userId) {
        return roomRepository.findByGroupId(groupId).stream()
                .filter(r -> participantRepository.existsByChatRoomIdAndUserIdAndStatus(r.getId(), userId, ParticipantStatus.ACTIVE))
                .map(r -> mapToRoomDto(r, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getRoomMessages(UUID roomId, UUID userId) {
        // Enforce backend authorization
        boolean isAuthorized = participantRepository.existsByChatRoomIdAndUserIdAndStatus(roomId, userId, ParticipantStatus.ACTIVE);
        if (!isAuthorized) {
            throw new SecurityException("Access denied: You are not an active participant in this chat room");
        }

        return messageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(this::mapToMessageDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageDto sendMessage(UUID roomId, UUID userId, SendMessageRequest request) {
        // Enforce backend authorization
        ChatParticipant participant = participantRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new SecurityException("Access denied: You are not in this chat room"));

        if (participant.getStatus() != ParticipantStatus.ACTIVE) {
            throw new SecurityException("Access denied: Participant is no longer active in this room");
        }

        User sender = participant.getUser();
        ChatRoom room = participant.getChatRoom();

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .senderUser(sender)
                .messageType(request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT)
                .messageText(request.getMessageText())
                .mediaUrl(request.getMediaUrl())
                .mediaFileName(request.getMediaFileName())
                .build();

        message = messageRepository.save(message);

        ChatMessageDto dto = mapToMessageDto(message);

        // Real-time broadcast to all subscribed room participants via STOMP
        try {
            messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, dto);
        } catch (Exception ex) {
            log.warn("Failed to broadcast message over websocket: {}", ex.getMessage());
        }

        return dto;
    }

    private ChatRoomDto mapToRoomDto(ChatRoom r, UUID viewerUserId) {
        int participantCount = participantRepository.findByChatRoomIdAndStatus(r.getId(), ParticipantStatus.ACTIVE).size();

        String title;
        if (r.getRoomType() == RoomType.MAIN_GROUP) {
            title = r.getGroup().getName() + " (Companion Chat)";
        } else {
            String orgName = r.getConnection() != null ? r.getConnection().getOrganizer().getOrganizerName() : "Organizer";
            title = r.getGroup().getName() + " ⇄ " + orgName;
        }

        return ChatRoomDto.builder()
                .id(r.getId())
                .groupId(r.getGroup().getId())
                .groupName(r.getGroup().getName())
                .connectionId(r.getConnection() != null ? r.getConnection().getId() : null)
                .roomType(r.getRoomType())
                .title(title)
                .participantCount(participantCount)
                .createdAt(r.getCreatedAt())
                .build();
    }

    private ChatMessageDto mapToMessageDto(ChatMessage m) {
        UserProfile profile = userProfileRepository.findByUserId(m.getSenderUser().getId()).orElse(null);
        String name = profile != null ? profile.getDisplayName() : m.getSenderUser().getEmail();

        ParticipantRole role = participantRepository.findByChatRoomIdAndUserId(m.getChatRoom().getId(), m.getSenderUser().getId())
                .map(ChatParticipant::getRole)
                .orElse(ParticipantRole.MEMBER);

        return ChatMessageDto.builder()
                .id(m.getId())
                .chatRoomId(m.getChatRoom().getId())
                .senderUserId(m.getSenderUser().getId())
                .senderDisplayName(name)
                .senderRole(role)
                .messageType(m.getMessageType())
                .messageText(m.getMessageText())
                .mediaUrl(m.getMediaUrl())
                .mediaFileName(m.getMediaFileName())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
