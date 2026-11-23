package com.thegreattreklores.group.dto;

import com.thegreattreklores.group.enums.GroupStatus;
import com.thegreattreklores.group.enums.GroupVisibility;
import com.thegreattreklores.group.enums.MembershipRole;
import com.thegreattreklores.group.enums.MembershipStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GroupDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGroupRequest {
        @NotBlank(message = "Group name is required")
        private String name;

        @NotBlank(message = "Destination is required")
        private String destination;

        private String description;

        private LocalDate preferredStartDate;

        private LocalDate preferredEndDate;

        @Builder.Default
        private Boolean datesFlexible = true;

        @Min(value = 2, message = "Group must allow at least 2 members")
        @Builder.Default
        private Integer maxMembers = 6;

        @Builder.Default
        private GroupVisibility visibility = GroupVisibility.PUBLIC;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupSummaryDto {
        private UUID id;
        private String name;
        private String destination;
        private String description;
        private LocalDate preferredStartDate;
        private LocalDate preferredEndDate;
        private Boolean datesFlexible;
        private Integer maxMembers;
        private Integer currentMemberCount;
        private GroupStatus status;
        private UUID createdByUserId;
        private String createdByName;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupDetailDto {
        private UUID id;
        private String name;
        private String destination;
        private String description;
        private LocalDate preferredStartDate;
        private LocalDate preferredEndDate;
        private Boolean datesFlexible;
        private Integer maxMembers;
        private GroupStatus status;
        private UUID createdByUserId;
        private String createdByName;
        private Boolean isCurrentUserAdmin;
        private MembershipStatus currentUserMembershipStatus;
        private List<MembershipDto> members;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipDto {
        private UUID membershipId;
        private UUID userId;
        private String displayName;
        private String profilePhotoUrl;
        private String experienceLevel;
        private MembershipRole role;
        private MembershipStatus status;
        private Instant joinedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMembershipRequest {
        private MembershipStatus status;
    }
}
