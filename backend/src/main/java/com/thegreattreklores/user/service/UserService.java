package com.thegreattreklores.user.service;

import com.thegreattreklores.user.dto.UserDtos.*;
import com.thegreattreklores.user.entity.User;
import com.thegreattreklores.user.entity.UserProfile;
import com.thegreattreklores.user.entity.UserSocialLink;
import com.thegreattreklores.user.entity.UserTrekHistory;
import com.thegreattreklores.user.enums.VisibilityLevel;
import com.thegreattreklores.user.repository.UserProfileRepository;
import com.thegreattreklores.user.repository.UserRepository;
import com.thegreattreklores.user.repository.UserSocialLinkRepository;
import com.thegreattreklores.user.repository.UserTrekHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSocialLinkRepository socialLinkRepository;
    private final UserTrekHistoryRepository trekHistoryRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID targetUserId, UUID viewerUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(targetUserId)
                .orElseGet(() -> UserProfile.builder()
                        .user(user)
                        .displayName(user.getEmail())
                        .build());

        boolean isSelf = viewerUserId != null && viewerUserId.equals(targetUserId);

        // Filter social links based on visibility
        List<SocialLinkDto> visibleLinks = socialLinkRepository.findByUserId(targetUserId).stream()
                .filter(link -> {
                    if (isSelf) return true;
                    if (link.getVisibility() == VisibilityLevel.PUBLIC) return true;
                    // For GROUP_MEMBERS: visible to logged-in users or group companions
                    if (link.getVisibility() == VisibilityLevel.GROUP_MEMBERS && viewerUserId != null) return true;
                    return false;
                })
                .map(link -> SocialLinkDto.builder()
                        .id(link.getId())
                        .platform(link.getPlatform())
                        .url(link.getUrl())
                        .visibility(link.getVisibility())
                        .build())
                .collect(Collectors.toList());

        List<TrekHistoryDto> trekHistory = trekHistoryRepository.findByUserIdOrderByStartDateDesc(targetUserId).stream()
                .map(t -> TrekHistoryDto.builder()
                        .id(t.getId())
                        .trekName(t.getTrekName())
                        .location(t.getLocation())
                        .startDate(t.getStartDate())
                        .endDate(t.getEndDate())
                        .description(t.getDescription())
                        .build())
                .collect(Collectors.toList());

        return ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .displayName(profile.getDisplayName())
                .profilePhotoUrl(profile.getProfilePhotoUrl())
                .location(profile.getLocation())
                .occupation(profile.getOccupation())
                .bio(profile.getBio())
                .trekkingExperienceLevel(profile.getTrekkingExperienceLevel())
                .socialLinks(visibleLinks)
                .trekHistory(trekHistory)
                .build();
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().user(user).displayName(user.getEmail()).build());

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            profile.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getProfilePhotoUrl() != null) {
            profile.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getOccupation() != null) {
            profile.setOccupation(request.getOccupation());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getTrekkingExperienceLevel() != null) {
            profile.setTrekkingExperienceLevel(request.getTrekkingExperienceLevel());
        }

        userProfileRepository.save(profile);
        return getProfile(userId, userId);
    }

    @Transactional
    public SocialLinkDto addSocialLink(UUID userId, CreateSocialLinkRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserSocialLink link = UserSocialLink.builder()
                .user(user)
                .platform(request.getPlatform())
                .url(request.getUrl())
                .visibility(request.getVisibility() != null ? request.getVisibility() : VisibilityLevel.PUBLIC)
                .build();

        link = socialLinkRepository.save(link);

        return SocialLinkDto.builder()
                .id(link.getId())
                .platform(link.getPlatform())
                .url(link.getUrl())
                .visibility(link.getVisibility())
                .build();
    }

    @Transactional
    public void deleteSocialLink(UUID userId, UUID linkId) {
        UserSocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Social link not found"));

        if (!link.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized to delete this link");
        }

        socialLinkRepository.delete(link);
    }

    @Transactional
    public TrekHistoryDto addTrekHistory(UUID userId, CreateTrekHistoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserTrekHistory trek = UserTrekHistory.builder()
                .user(user)
                .trekName(request.getTrekName())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .build();

        trek = trekHistoryRepository.save(trek);

        return TrekHistoryDto.builder()
                .id(trek.getId())
                .trekName(trek.getTrekName())
                .location(trek.getLocation())
                .startDate(trek.getStartDate())
                .endDate(trek.getEndDate())
                .description(trek.getDescription())
                .build();
    }
}
