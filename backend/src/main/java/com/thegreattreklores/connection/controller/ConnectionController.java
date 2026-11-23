package com.thegreattreklores.connection.controller;

import com.thegreattreklores.auth.dto.UserPrincipal;
import com.thegreattreklores.common.api.ApiResponse;
import com.thegreattreklores.connection.dto.ConnectionDtos.*;
import com.thegreattreklores.connection.service.ConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
@Tag(name = "Group-Organizer Connections", description = "Endpoints for negotiating and matchmaking between groups and organizers")
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/groups/{groupId}/enquire/{organizerId}")
    @Operation(summary = "Group enquires with Organizer", description = "Group Admin sends an enquiry to an organizer")
    public ResponseEntity<ApiResponse<ConnectionDto>> enquireWithOrganizer(
            @PathVariable UUID groupId,
            @PathVariable UUID organizerId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) CreateConnectionRequest request) {
        String msg = request != null ? request.getMessage() : null;
        ConnectionDto response = connectionService.createGroupEnquiry(groupId, organizerId, currentUser.getId(), msg);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enquiry sent to organizer", response));
    }

    @PostMapping("/organizers/{organizerId}/pitch/{groupId}")
    @Operation(summary = "Organizer pitches to Group", description = "Organizer pitches departure package to an open companion group")
    public ResponseEntity<ApiResponse<ConnectionDto>> pitchToGroup(
            @PathVariable UUID organizerId,
            @PathVariable UUID groupId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) CreateConnectionRequest request) {
        String msg = request != null ? request.getMessage() : null;
        ConnectionDto response = connectionService.createOrganizerPitch(organizerId, groupId, currentUser.getId(), msg);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pitch sent to group", response));
    }

    @PatchMapping("/{connectionId}/status")
    @Operation(summary = "Update connection status", description = "Accept, reject, confirm, or close a connection")
    public ResponseEntity<ApiResponse<ConnectionDto>> updateStatus(
            @PathVariable UUID connectionId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateConnectionStatusRequest request) {
        ConnectionDto response = connectionService.updateConnectionStatus(connectionId, currentUser.getId(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Connection status updated to " + request.getStatus(), response));
    }

    @GetMapping("/groups/{groupId}")
    @Operation(summary = "List connections for a group", description = "Allows group companions to see active organizer enquiries")
    public ResponseEntity<ApiResponse<List<ConnectionDto>>> getGroupConnections(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ConnectionDto> response = connectionService.getGroupConnections(groupId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/organizer/me")
    @Operation(summary = "List connections for current organizer", description = "View incoming enquiries and active pitches")
    public ResponseEntity<ApiResponse<List<ConnectionDto>>> getMyOrganizerConnections(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ConnectionDto> response = connectionService.getOrganizerConnections(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
