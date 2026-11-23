package com.thegreattreklores.connection.service;

import com.thegreattreklores.connection.dto.ConnectionDtos.*;
import com.thegreattreklores.connection.entity.GroupOrganizerConnection;
import com.thegreattreklores.connection.enums.ConnectionInitiator;
import com.thegreattreklores.connection.enums.ConnectionStatus;
import com.thegreattreklores.connection.repository.GroupOrganizerConnectionRepository;
import com.thegreattreklores.group.entity.GroupMembership;
import com.thegreattreklores.group.entity.TripGroup;
import com.thegreattreklores.group.enums.MembershipRole;
import com.thegreattreklores.group.enums.MembershipStatus;
import com.thegreattreklores.group.repository.GroupMembershipRepository;
import com.thegreattreklores.group.repository.TripGroupRepository;
import com.thegreattreklores.organizer.entity.Organizer;
import com.thegreattreklores.organizer.repository.OrganizerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final GroupOrganizerConnectionRepository connectionRepository;
    private final TripGroupRepository groupRepository;
    private final OrganizerRepository organizerRepository;
    private final GroupMembershipRepository membershipRepository;
    private final com.thegreattreklores.chat.service.ChatService chatService;

    @Transactional
    public ConnectionDto createGroupEnquiry(UUID groupId, UUID organizerId, UUID adminUserId, String message) {
        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Trip group not found"));

        GroupMembership adminCheck = membershipRepository.findByGroupIdAndUserId(groupId, adminUserId)
                .orElseThrow(() -> new SecurityException("Unauthorized"));

        if (adminCheck.getRole() != MembershipRole.ADMIN) {
            throw new SecurityException("Only group admins can send organizer enquiries");
        }

        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        Optional<GroupOrganizerConnection> existing = connectionRepository.findByGroupIdAndOrganizerId(groupId, organizerId);
        if (existing.isPresent()) {
            GroupOrganizerConnection c = existing.get();
            if (c.getStatus() == ConnectionStatus.PENDING || c.getStatus() == ConnectionStatus.ACCEPTED || c.getStatus() == ConnectionStatus.CONFIRMED) {
                throw new IllegalStateException("A connection is already active with this organizer");
            }
            c.setInitiatedBy(ConnectionInitiator.GROUP);
            c.setMessage(message);
            c.setStatus(ConnectionStatus.PENDING);
            return mapToDto(connectionRepository.save(c));
        }

        GroupOrganizerConnection connection = GroupOrganizerConnection.builder()
                .group(group)
                .organizer(organizer)
                .initiatedBy(ConnectionInitiator.GROUP)
                .message(message != null ? message.trim() : "Hello, we are interested in discussing trek plans with your team.")
                .status(ConnectionStatus.PENDING)
                .build();

        return mapToDto(connectionRepository.save(connection));
    }

    @Transactional
    public ConnectionDto createOrganizerPitch(UUID organizerId, UUID groupId, UUID ownerUserId, String message) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        if (!organizer.getOwnerUser().getId().equals(ownerUserId)) {
            throw new SecurityException("Unauthorized to pitch for this organizer");
        }

        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Trip group not found"));

        Optional<GroupOrganizerConnection> existing = connectionRepository.findByGroupIdAndOrganizerId(groupId, organizerId);
        if (existing.isPresent()) {
            GroupOrganizerConnection c = existing.get();
            if (c.getStatus() == ConnectionStatus.PENDING || c.getStatus() == ConnectionStatus.ACCEPTED || c.getStatus() == ConnectionStatus.CONFIRMED) {
                throw new IllegalStateException("A connection is already active with this group");
            }
            c.setInitiatedBy(ConnectionInitiator.ORGANIZER);
            c.setMessage(message);
            c.setStatus(ConnectionStatus.PENDING);
            return mapToDto(connectionRepository.save(c));
        }

        GroupOrganizerConnection connection = GroupOrganizerConnection.builder()
                .group(group)
                .organizer(organizer)
                .initiatedBy(ConnectionInitiator.ORGANIZER)
                .message(message != null ? message.trim() : "Hello! We have departures matching your destination and target dates.")
                .status(ConnectionStatus.PENDING)
                .build();

        return mapToDto(connectionRepository.save(connection));
    }

    @Transactional
    public ConnectionDto updateConnectionStatus(UUID connectionId, UUID currentUserId, ConnectionStatus newStatus) {
        GroupOrganizerConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found"));

        boolean isOrganizerOwner = connection.getOrganizer().getOwnerUser().getId().equals(currentUserId);
        boolean isGroupAdmin = membershipRepository.findByGroupIdAndUserId(connection.getGroup().getId(), currentUserId)
                .map(m -> m.getRole() == MembershipRole.ADMIN)
                .orElse(false);

        if (!isOrganizerOwner && !isGroupAdmin) {
            throw new SecurityException("Unauthorized to update this connection");
        }

        if (newStatus == ConnectionStatus.ACCEPTED) {
            if (connection.getInitiatedBy() == ConnectionInitiator.GROUP && !isOrganizerOwner) {
                throw new SecurityException("Only the organizer can accept an incoming group enquiry");
            }
            if (connection.getInitiatedBy() == ConnectionInitiator.ORGANIZER && !isGroupAdmin) {
                throw new SecurityException("Only the group admin can accept an organizer pitch");
            }
            // Auto-provision ORGANIZER_DISCUSSION room
            chatService.provisionOrganizerDiscussionRoom(connection);
        }

        if (newStatus == ConnectionStatus.CONFIRMED) {
            if (!isGroupAdmin) {
                throw new SecurityException("Only the group admin can confirm an organizer selection");
            }
        }

        connection.setStatus(newStatus);
        return mapToDto(connectionRepository.save(connection));
    }

    @Transactional(readOnly = true)
    public List<ConnectionDto> getGroupConnections(UUID groupId, UUID currentUserId) {
        boolean isMember = membershipRepository.findByGroupIdAndUserId(groupId, currentUserId)
                .map(m -> m.getStatus() == MembershipStatus.ACCEPTED)
                .orElse(false);

        if (!isMember) {
            throw new SecurityException("Only group companions can view organizer connections");
        }

        return connectionRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConnectionDto> getOrganizerConnections(UUID currentUserId) {
        Organizer organizer = organizerRepository.findByOwnerUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer profile not found"));

        return connectionRepository.findByOrganizerIdOrderByCreatedAtDesc(organizer.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ConnectionDto mapToDto(GroupOrganizerConnection c) {
        return ConnectionDto.builder()
                .id(c.getId())
                .groupId(c.getGroup().getId())
                .groupName(c.getGroup().getName())
                .groupDestination(c.getGroup().getDestination())
                .groupStartDate(c.getGroup().getPreferredStartDate())
                .groupEndDate(c.getGroup().getPreferredEndDate())
                .organizerId(c.getOrganizer().getId())
                .organizerName(c.getOrganizer().getOrganizerName())
                .organizerLocation(c.getOrganizer().getLocation())
                .initiatedBy(c.getInitiatedBy())
                .message(c.getMessage())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
