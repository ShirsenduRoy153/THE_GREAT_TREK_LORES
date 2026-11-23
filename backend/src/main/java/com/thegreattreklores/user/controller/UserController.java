package com.thegreattreklores.user.controller;

import com.thegreattreklores.auth.dto.UserPrincipal;
import com.thegreattreklores.common.api.ApiResponse;
import com.thegreattreklores.user.dto.UserDtos.*;
import com.thegreattreklores.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "Endpoints for viewing and updating user profiles, social links, and trek history")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get user profile", description = "Fetches public profile with privacy-aware visibility filtering")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UUID viewerId = currentUser != null ? currentUser.getId() : null;
        ProfileResponse response = userService.getProfile(userId, viewerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/profile")
    @Operation(summary = "Get own profile", description = "Fetches the full profile for the logged in user")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        ProfileResponse response = userService.getProfile(currentUser.getId(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Update profile", description = "Updates details like display name, bio, experience level, location")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody UpdateProfileRequest request) {
        ProfileResponse response = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PostMapping("/me/social-links")
    @Operation(summary = "Add social link", description = "Adds a social link (Instagram, WhatsApp, etc.) with visibility settings")
    public ResponseEntity<ApiResponse<SocialLinkDto>> addSocialLink(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody CreateSocialLinkRequest request) {
        SocialLinkDto response = userService.addSocialLink(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Social link added", response));
    }

    @DeleteMapping("/me/social-links/{linkId}")
    @Operation(summary = "Delete social link", description = "Removes a specific social link")
    public ResponseEntity<ApiResponse<Void>> deleteSocialLink(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID linkId) {
        userService.deleteSocialLink(currentUser.getId(), linkId);
        return ResponseEntity.ok(ApiResponse.success("Social link removed", null));
    }

    @PostMapping("/me/trek-history")
    @Operation(summary = "Add trek history", description = "Adds a completed trek to the user's profile history")
    public ResponseEntity<ApiResponse<TrekHistoryDto>> addTrekHistory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody CreateTrekHistoryRequest request) {
        TrekHistoryDto response = userService.addTrekHistory(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trek history added", response));
    }
}
