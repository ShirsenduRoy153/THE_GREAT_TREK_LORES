package com.thegreattreklores.user.dto;

import com.thegreattreklores.user.enums.ExperienceLevel;
import com.thegreattreklores.user.enums.SocialPlatform;
import com.thegreattreklores.user.enums.UserRole;
import com.thegreattreklores.user.enums.VisibilityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class UserDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileResponse {
        private UUID userId;
        private String email;
        private UserRole role;
        private String displayName;
        private String profilePhotoUrl;
        private String location;
        private String occupation;
        private String bio;
        private ExperienceLevel trekkingExperienceLevel;
        private List<SocialLinkDto> socialLinks;
        private List<TrekHistoryDto> trekHistory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {
        private String displayName;
        private String profilePhotoUrl;
        private String location;
        private String occupation;
        private String bio;
        private ExperienceLevel trekkingExperienceLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialLinkDto {
        private UUID id;
        private SocialPlatform platform;
        private String url;
        private VisibilityLevel visibility;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSocialLinkRequest {
        private SocialPlatform platform;
        private String url;
        private VisibilityLevel visibility;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrekHistoryDto {
        private UUID id;
        private String trekName;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTrekHistoryRequest {
        private String trekName;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
    }
}
