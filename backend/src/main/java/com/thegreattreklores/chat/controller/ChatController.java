package com.thegreattreklores.chat.controller;

import com.thegreattreklores.auth.dto.UserPrincipal;
import com.thegreattreklores.chat.dto.ChatDtos.*;
import com.thegreattreklores.chat.service.ChatService;
import com.thegreattreklores.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat & Messaging", description = "Endpoints for dual-room chat navigation and message history")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/api/chat/rooms")
    @Operation(summary = "Get user chat rooms", description = "Returns all active group chats and organizer discussion rooms accessible to current user")
    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getUserRooms(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ChatRoomDto> response = chatService.getUserRooms(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/chat/rooms/groups/{groupId}")
    @Operation(summary = "Get chat rooms for a group", description = "Returns main room and organizer rooms for a specific trip group")
    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getGroupRooms(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ChatRoomDto> response = chatService.getGroupRooms(groupId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/chat/rooms/{roomId}/messages")
    @Operation(summary = "Get room message history", description = "Strictly enforces participant authorization before returning messages")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getRoomMessages(
            @PathVariable UUID roomId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ChatMessageDto> response = chatService.getRoomMessages(roomId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/chat/rooms/{roomId}/messages")
    @Operation(summary = "Send message (REST)", description = "Sends message via REST and automatically broadcasts to live WebSocket subscribers")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendMessage(
            @PathVariable UUID roomId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody SendMessageRequest request) {
        ChatMessageDto response = chatService.sendMessage(roomId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @MessageMapping("/chat.sendMessage")
    public void sendSocketMessage(@Payload SocketMessagePayload payload, Principal principal) {
        if (principal == null) return;
        UUID senderId = UUID.fromString(principal.getName());
        SendMessageRequest request = SendMessageRequest.builder()
                .messageText(payload.getMessageText())
                .messageType(payload.getMessageType())
                .mediaUrl(payload.getMediaUrl())
                .build();
        chatService.sendMessage(payload.getRoomId(), senderId, request);
    }
}
