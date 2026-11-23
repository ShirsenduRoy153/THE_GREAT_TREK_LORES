package com.thegreattreklores.trek.controller;

import com.thegreattreklores.auth.dto.UserPrincipal;
import com.thegreattreklores.common.api.ApiResponse;
import com.thegreattreklores.trek.dto.OrganizerTrekDtos.*;
import com.thegreattreklores.trek.service.TrekService;
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
@RequiredArgsConstructor
@Tag(name = "Treks & Organizers", description = "Endpoints for publishing treks, scheduled departure batches, and date matching")
public class TrekController {

    private final TrekService trekService;

    @PostMapping("/api/organizers")
    @Operation(summary = "Register or update organizer profile")
    public ResponseEntity<ApiResponse<OrganizerDto>> setupOrganizer(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateOrganizerRequest request) {
        OrganizerDto response = trekService.registerOrUpdateOrganizer(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Organizer profile saved", response));
    }

    @GetMapping("/api/organizers/me")
    @Operation(summary = "Get current organizer profile")
    public ResponseEntity<ApiResponse<OrganizerDto>> getMyOrganizerProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        OrganizerDto response = trekService.getOrganizerByOwner(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/treks")
    @Operation(summary = "Publish trek listing", description = "Allows organizers to publish a trek package")
    public ResponseEntity<ApiResponse<TrekListingSummaryDto>> createTrek(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateTrekListingRequest request) {
        TrekListingSummaryDto response = trekService.createTrekListing(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trek listing published", response));
    }

    @GetMapping("/api/treks")
    @Operation(summary = "Search treks", description = "Browse trek packages by destination")
    public ResponseEntity<ApiResponse<List<TrekListingSummaryDto>>> searchTreks(
            @RequestParam(required = false) String destination) {
        List<TrekListingSummaryDto> response = trekService.searchTreks(destination);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/treks/{trekId}")
    @Operation(summary = "Get trek details", description = "View trek details along with all scheduled departure batches")
    public ResponseEntity<ApiResponse<TrekListingDetailDto>> getTrekDetails(@PathVariable UUID trekId) {
        TrekListingDetailDto response = trekService.getTrekDetails(trekId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/treks/{trekId}/departures")
    @Operation(summary = "Add departure batch", description = "Organizer schedules a specific departure date range with capacity")
    public ResponseEntity<ApiResponse<DepartureDto>> createDeparture(
            @PathVariable UUID trekId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateDepartureRequest request) {
        DepartureDto response = trekService.createDeparture(currentUser.getId(), trekId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Departure scheduled successfully", response));
    }

    @GetMapping("/api/treks/matching-departures")
    @Operation(summary = "Find matching departures for a group", description = "Matches departures whose dates overlap with group dates")
    public ResponseEntity<ApiResponse<List<DepartureDto>>> findMatchingDepartures(@RequestParam UUID groupId) {
        List<DepartureDto> response = trekService.findMatchingDeparturesForGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
