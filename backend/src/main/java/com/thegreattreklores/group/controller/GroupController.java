package com.thegreattreklores.group.controller;

import com.thegreattreklores.auth.dto.UserPrincipal;
import com.thegreattreklores.common.api.ApiResponse;
import com.thegreattreklores.group.dto.GroupDtos.*;
import com.thegreattreklores.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Trip Groups", description = "Endpoints for creating and discovering companion groups, and managing join requests")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Create trip group", description = "Creates a companion group; creator becomes Group Admin")
    public ResponseEntity<ApiResponse<GroupSummaryDto>> createGroup(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateGroupRequest request) {
        GroupSummaryDto response = groupService.createGroup(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip group created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Search groups", description = "Browse open companion groups by destination and target dates")
    public ResponseEntity<ApiResponse<List<GroupSummaryDto>>> searchGroups(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<GroupSummaryDto> response = groupService.searchGroups(destination, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get group details", description = "Fetches group overview, current member roster, and join status")
    public ResponseEntity<ApiResponse<GroupDetailDto>> getGroupDetails(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UUID viewerId = currentUser != null ? currentUser.getId() : null;
        GroupDetailDto response = groupService.getGroupDetails(groupId, viewerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{groupId}/join-requests")
    @Operation(summary = "Request to join", description = "Submits a join request to the group admin")
    public ResponseEntity<ApiResponse<MembershipDto>> requestToJoin(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MembershipDto response = groupService.requestToJoin(groupId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Join request submitted", response));
    }

    @PatchMapping("/{groupId}/memberships/{membershipId}")
    @Operation(summary = "Manage join request", description = "Group Admin approves (ACCEPTED) or rejects (REJECTED) a member")
    public ResponseEntity<ApiResponse<MembershipDto>> manageMembership(
            @PathVariable UUID groupId,
            @PathVariable UUID membershipId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody UpdateMembershipRequest request) {
        MembershipDto response = groupService.manageMembership(groupId, membershipId, currentUser.getId(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Membership updated", response));
    }
}
