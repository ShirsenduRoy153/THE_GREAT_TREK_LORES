package com.thegreattreklores.group.service;

import com.thegreattreklores.group.dto.GroupDtos.*;
import com.thegreattreklores.group.entity.GroupMembership;
import com.thegreattreklores.group.entity.TripGroup;
import com.thegreattreklores.group.enums.GroupStatus;
import com.thegreattreklores.group.enums.MembershipRole;
import com.thegreattreklores.group.enums.MembershipStatus;
import com.thegreattreklores.group.repository.GroupMembershipRepository;
import com.thegreattreklores.group.repository.TripGroupRepository;
import com.thegreattreklores.user.entity.User;
import com.thegreattreklores.user.entity.UserProfile;
import com.thegreattreklores.user.repository.UserProfileRepository;
import com.thegreattreklores.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final TripGroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final com.thegreattreklores.chat.service.ChatService chatService;
    private final com.thegreattreklores.notification.service.NotificationService notificationService;

    @Transactional
    public GroupSummaryDto createGroup(UUID userId, CreateGroupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TripGroup group = TripGroup.builder()
                .createdByUser(user)
                .name(request.getName().trim())
                .destination(request.getDestination().trim())
                .description(request.getDescription())
                .preferredStartDate(request.getPreferredStartDate())
                .preferredEndDate(request.getPreferredEndDate())
                .datesFlexible(request.getDatesFlexible() != null ? request.getDatesFlexible() : true)
                .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 6)
                .visibility(request.getVisibility())
                .status(GroupStatus.OPEN)
                .build();

        group = groupRepository.save(group);

        // Creator is automatically the Group Admin and ACCEPTED member
        GroupMembership adminMembership = GroupMembership.builder()
                .group(group)
                .user(user)
                .role(MembershipRole.ADMIN)
                .status(MembershipStatus.ACCEPTED)
                .joinedAt(Instant.now())
                .build();

        membershipRepository.save(adminMembership);

        // Automatically provision private MAIN_GROUP chat room
        chatService.provisionMainGroupRoom(group, user);

        String creatorName = userProfileRepository.findByUserId(userId)
                .map(UserProfile::getDisplayName)
                .orElse(user.getEmail());

        return mapToSummary(group, 1, creatorName);
    }

    @Transactional(readOnly = true)
    public List<GroupSummaryDto> searchGroups(String destination, LocalDate startDate, LocalDate endDate) {
        List<TripGroup> groups = groupRepository.searchGroups(GroupStatus.OPEN, destination, startDate, endDate);

        return groups.stream().map(g -> {
            int memberCount = membershipRepository.countByGroupIdAndStatus(g.getId(), MembershipStatus.ACCEPTED);
            String creatorName = userProfileRepository.findByUserId(g.getCreatedByUser().getId())
                    .map(UserProfile::getDisplayName)
                    .orElse(g.getCreatedByUser().getEmail());
            return mapToSummary(g, memberCount, creatorName);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupDetailDto getGroupDetails(UUID groupId, UUID viewerUserId) {
        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Trip group not found"));

        List<GroupMembership> allMemberships = membershipRepository.findByGroupId(groupId);

        boolean isViewerAdmin = false;
        MembershipStatus viewerStatus = null;

        if (viewerUserId != null) {
            Optional<GroupMembership> viewerMembership = allMemberships.stream()
                    .filter(m -> m.getUser().getId().equals(viewerUserId))
                    .findFirst();

            if (viewerMembership.isPresent()) {
                viewerStatus = viewerMembership.get().getStatus();
                isViewerAdmin = viewerMembership.get().getRole() == MembershipRole.ADMIN;
            }
        }

        final boolean canViewPending = isViewerAdmin;

        List<MembershipDto> memberDtos = allMemberships.stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACCEPTED || (canViewPending && m.getStatus() == MembershipStatus.PENDING))
                .map(m -> {
                    UserProfile profile = userProfileRepository.findByUserId(m.getUser().getId()).orElse(null);
                    return MembershipDto.builder()
                            .membershipId(m.getId())
                            .userId(m.getUser().getId())
                            .displayName(profile != null ? profile.getDisplayName() : m.getUser().getEmail())
                            .profilePhotoUrl(profile != null ? profile.getProfilePhotoUrl() : null)
                            .experienceLevel(profile != null && profile.getTrekkingExperienceLevel() != null
                                    ? profile.getTrekkingExperienceLevel().name() : "BEGINNER")
                            .role(m.getRole())
                            .status(m.getStatus())
                            .joinedAt(m.getJoinedAt())
                            .build();
                })
                .collect(Collectors.toList());

        String creatorName = userProfileRepository.findByUserId(group.getCreatedByUser().getId())
                .map(UserProfile::getDisplayName)
                .orElse(group.getCreatedByUser().getEmail());

        return GroupDetailDto.builder()
                .id(group.getId())
                .name(group.getName())
                .destination(group.getDestination())
                .description(group.getDescription())
                .preferredStartDate(group.getPreferredStartDate())
                .preferredEndDate(group.getPreferredEndDate())
                .datesFlexible(group.getDatesFlexible())
                .maxMembers(group.getMaxMembers())
                .status(group.getStatus())
                .createdByUserId(group.getCreatedByUser().getId())
                .createdByName(creatorName)
                .isCurrentUserAdmin(isViewerAdmin)
                .currentUserMembershipStatus(viewerStatus)
                .members(memberDtos)
                .createdAt(group.getCreatedAt())
                .build();
    }

    @Transactional
    public MembershipDto requestToJoin(UUID groupId, UUID userId) {
        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (group.getStatus() != GroupStatus.OPEN) {
            throw new IllegalStateException("This group is not accepting new members");
        }

        int currentMembers = membershipRepository.countByGroupIdAndStatus(groupId, MembershipStatus.ACCEPTED);
        if (currentMembers >= group.getMaxMembers()) {
            throw new IllegalStateException("Group has reached its maximum capacity");
        }

        Optional<GroupMembership> existing = membershipRepository.findByGroupIdAndUserId(groupId, userId);
        if (existing.isPresent()) {
            GroupMembership m = existing.get();
            if (m.getStatus() == MembershipStatus.ACCEPTED) {
                throw new IllegalStateException("You are already an accepted member of this group");
            }
            if (m.getStatus() == MembershipStatus.PENDING) {
                throw new IllegalStateException("You already have a pending join request");
            }
            // If previously left or rejected, allow re-requesting
            m.setStatus(MembershipStatus.PENDING);
            m = membershipRepository.save(m);
            return mapToMembershipDto(m);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        GroupMembership membership = GroupMembership.builder()
                .group(group)
                .user(user)
                .role(MembershipRole.MEMBER)
                .status(MembershipStatus.PENDING)
                .build();

        membership = membershipRepository.save(membership);

        // Notify Group Admin
        notificationService.sendNotification(
                group.getCreatedByUser(),
                com.thegreattreklores.notification.enums.NotificationType.JOIN_REQUEST,
                "New Join Request",
                user.getEmail() + " requested to join " + group.getName(),
                "GROUP",
                group.getId()
        );

        return mapToMembershipDto(membership);
    }

    @Transactional
    public MembershipDto manageMembership(UUID groupId, UUID membershipId, UUID adminUserId, MembershipStatus newStatus) {
        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Verify that adminUserId is indeed group admin
        GroupMembership adminCheck = membershipRepository.findByGroupIdAndUserId(groupId, adminUserId)
                .orElseThrow(() -> new SecurityException("Unauthorized"));

        if (adminCheck.getRole() != MembershipRole.ADMIN) {
            throw new SecurityException("Only group admins can manage member requests");
        }

        GroupMembership target = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membership record not found"));

        if (!target.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Membership does not belong to this group");
        }

        target.setStatus(newStatus);
        if (newStatus == MembershipStatus.ACCEPTED) {
            target.setJoinedAt(Instant.now());
            chatService.addMemberToGroupRooms(groupId, target.getUser(), com.thegreattreklores.chat.enums.ParticipantRole.MEMBER);
            notificationService.sendNotification(
                    target.getUser(),
                    com.thegreattreklores.notification.enums.NotificationType.JOIN_ACCEPTED,
                    "Join Request Approved!",
                    "You are now a companion in " + group.getName() + ". You can now chat in the private group room!",
                    "GROUP",
                    group.getId()
            );
        } else if (newStatus == MembershipStatus.REJECTED || newStatus == MembershipStatus.REMOVED || newStatus == MembershipStatus.LEFT) {
            chatService.removeMemberFromGroupRooms(groupId, target.getUser().getId());
        }

        target = membershipRepository.save(target);
        return mapToMembershipDto(target);
    }

    private GroupSummaryDto mapToSummary(TripGroup g, int count, String creatorName) {
        return GroupSummaryDto.builder()
                .id(g.getId())
                .name(g.getName())
                .destination(g.getDestination())
                .description(g.getDescription())
                .preferredStartDate(g.getPreferredStartDate())
                .preferredEndDate(g.getPreferredEndDate())
                .datesFlexible(g.getDatesFlexible())
                .maxMembers(g.getMaxMembers())
                .currentMemberCount(count)
                .status(g.getStatus())
                .createdByUserId(g.getCreatedByUser().getId())
                .createdByName(creatorName)
                .createdAt(g.getCreatedAt())
                .build();
    }

    private MembershipDto mapToMembershipDto(GroupMembership m) {
        UserProfile profile = userProfileRepository.findByUserId(m.getUser().getId()).orElse(null);
        return MembershipDto.builder()
                .membershipId(m.getId())
                .userId(m.getUser().getId())
                .displayName(profile != null ? profile.getDisplayName() : m.getUser().getEmail())
                .profilePhotoUrl(profile != null ? profile.getProfilePhotoUrl() : null)
                .experienceLevel(profile != null && profile.getTrekkingExperienceLevel() != null
                        ? profile.getTrekkingExperienceLevel().name() : "BEGINNER")
                .role(m.getRole())
                .status(m.getStatus())
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
