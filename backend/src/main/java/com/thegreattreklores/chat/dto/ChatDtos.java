package com.thegreattreklores.chat.dto;

import com.thegreattreklores.chat.enums.MessageType;
import com.thegreattreklores.chat.enums.ParticipantRole;
import com.thegreattreklores.chat.enums.RoomType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

public class ChatDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomDto {
        private UUID id;
        private UUID groupId;
        private String groupName;
        private UUID connectionId;
        private RoomType roomType;
        private String title;
        private int participantCount;
        private String lastMessage;
        private Instant lastMessageTime;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageDto {
        private UUID id;
        private UUID chatRoomId;
        private UUID senderUserId;
        private String senderDisplayName;
        private ParticipantRole senderRole;
        private MessageType messageType;
        private String messageText;
        private String mediaUrl;
        private String mediaFileName;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageRequest {
        private String messageText;
        @Builder.Default
        private MessageType messageType = MessageType.TEXT;
        private String mediaUrl;
        private String mediaFileName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocketMessagePayload {
        @NotNull
        private UUID roomId;
        private String messageText;
        private MessageType messageType;
        private String mediaUrl;
    }
}
