package com.thegreattreklores.trek.service;

import com.thegreattreklores.group.entity.TripGroup;
import com.thegreattreklores.group.repository.TripGroupRepository;
import com.thegreattreklores.organizer.entity.Organizer;
import com.thegreattreklores.organizer.enums.VerificationStatus;
import com.thegreattreklores.organizer.repository.OrganizerRepository;
import com.thegreattreklores.trek.dto.OrganizerTrekDtos.*;
import com.thegreattreklores.trek.entity.TrekDeparture;
import com.thegreattreklores.trek.entity.TrekListing;
import com.thegreattreklores.trek.enums.DepartureStatus;
import com.thegreattreklores.trek.enums.ListingStatus;
import com.thegreattreklores.trek.repository.TrekDepartureRepository;
import com.thegreattreklores.trek.repository.TrekListingRepository;
import com.thegreattreklores.user.entity.User;
import com.thegreattreklores.user.enums.UserRole;
import com.thegreattreklores.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrekService {

    private final OrganizerRepository organizerRepository;
    private final TrekListingRepository listingRepository;
    private final TrekDepartureRepository departureRepository;
    private final TripGroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrganizerDto registerOrUpdateOrganizer(UUID userId, CreateOrganizerRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRole(UserRole.ORGANIZER);
        userRepository.save(user);

        Organizer organizer = organizerRepository.findByOwnerUserId(userId)
                .orElseGet(() -> Organizer.builder().ownerUser(user).build());

        organizer.setOrganizerName(request.getOrganizerName().trim());
        organizer.setDescription(request.getDescription());
        organizer.setLogoUrl(request.getLogoUrl());
        organizer.setLocation(request.getLocation());
        organizer.setWebsiteUrl(request.getWebsiteUrl());
        organizer.setVerificationStatus(VerificationStatus.VERIFIED); // Auto-verify for demo MVP

        organizer = organizerRepository.save(organizer);

        return mapToOrganizerDto(organizer);
    }

    @Transactional(readOnly = true)
    public OrganizerDto getOrganizerByOwner(UUID userId) {
        Organizer organizer = organizerRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer profile not found"));
        return mapToOrganizerDto(organizer);
    }

    @Transactional
    public TrekListingSummaryDto createTrekListing(UUID userId, CreateTrekListingRequest request) {
        Organizer organizer = organizerRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Only registered organizers can publish treks"));

        TrekListing listing = TrekListing.builder()
                .organizer(organizer)
                .title(request.getTitle().trim())
                .destination(request.getDestination().trim())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .durationDays(request.getDurationDays())
                .startingPrice(request.getStartingPrice())
                .coverImageUrl(request.getCoverImageUrl())
                .listingStatus(ListingStatus.PUBLISHED)
                .build();

        listing = listingRepository.save(listing);
        return mapToSummary(listing, 0);
    }

    @Transactional(readOnly = true)
    public List<TrekListingSummaryDto> searchTreks(String destination) {
        List<TrekListing> listings;
        if (destination != null && !destination.isBlank()) {
            listings = listingRepository.findByDestinationContainingIgnoreCaseAndListingStatusOrderByCreatedAtDesc(
                    destination.trim(), ListingStatus.PUBLISHED);
        } else {
            listings = listingRepository.findByListingStatusOrderByCreatedAtDesc(ListingStatus.PUBLISHED);
        }

        return listings.stream().map(l -> {
            int departures = departureRepository.findByTrekListingIdAndDepartureStatusOrderByStartDateAsc(
                    l.getId(), DepartureStatus.OPEN).size();
            return mapToSummary(l, departures);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrekListingDetailDto getTrekDetails(UUID trekId) {
        TrekListing listing = listingRepository.findById(trekId)
                .orElseThrow(() -> new IllegalArgumentException("Trek listing not found"));

        List<DepartureDto> departures = departureRepository.findByTrekListingIdOrderByStartDateAsc(trekId)
                .stream()
                .map(this::mapToDepartureDto)
                .collect(Collectors.toList());

        return TrekListingDetailDto.builder()
                .id(listing.getId())
                .organizer(mapToOrganizerDto(listing.getOrganizer()))
                .title(listing.getTitle())
                .destination(listing.getDestination())
                .description(listing.getDescription())
                .difficulty(listing.getDifficulty())
                .durationDays(listing.getDurationDays())
                .startingPrice(listing.getStartingPrice())
                .coverImageUrl(listing.getCoverImageUrl())
                .listingStatus(listing.getListingStatus())
                .departures(departures)
                .build();
    }

    @Transactional
    public DepartureDto createDeparture(UUID userId, UUID trekListingId, CreateDepartureRequest request) {
        TrekListing listing = listingRepository.findById(trekListingId)
                .orElseThrow(() -> new IllegalArgumentException("Trek listing not found"));

        if (!listing.getOrganizer().getOwnerUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized to add departures for this trek");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        TrekDeparture departure = TrekDeparture.builder()
                .trekListing(listing)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .price(request.getPrice())
                .capacity(request.getCapacity())
                .availableSlots(request.getCapacity())
                .departureStatus(DepartureStatus.OPEN)
                .build();

        departure = departureRepository.save(departure);
        return mapToDepartureDto(departure);
    }

    @Transactional(readOnly = true)
    public List<DepartureDto> findMatchingDeparturesForGroup(UUID groupId) {
        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (group.getPreferredStartDate() == null || group.getPreferredEndDate() == null) {
            return List.of();
        }

        // Match overlap query
        return departureRepository.findOverlappingDepartures(
                group.getDestination(),
                group.getPreferredStartDate(),
                group.getPreferredEndDate())
                .stream()
                .map(this::mapToDepartureDto)
                .collect(Collectors.toList());
    }

    private OrganizerDto mapToOrganizerDto(Organizer o) {
        return OrganizerDto.builder()
                .id(o.getId())
                .ownerUserId(o.getOwnerUser().getId())
                .organizerName(o.getOrganizerName())
                .description(o.getDescription())
                .logoUrl(o.getLogoUrl())
                .location(o.getLocation())
                .websiteUrl(o.getWebsiteUrl())
                .verificationStatus(o.getVerificationStatus())
                .build();
    }

    private TrekListingSummaryDto mapToSummary(TrekListing l, int count) {
        return TrekListingSummaryDto.builder()
                .id(l.getId())
                .organizerId(l.getOrganizer().getId())
                .organizerName(l.getOrganizer().getOrganizerName())
                .title(l.getTitle())
                .destination(l.getDestination())
                .description(l.getDescription())
                .difficulty(l.getDifficulty())
                .durationDays(l.getDurationDays())
                .startingPrice(l.getStartingPrice())
                .coverImageUrl(l.getCoverImageUrl())
                .listingStatus(l.getListingStatus())
                .upcomingDepartureCount(count)
                .build();
    }

    private DepartureDto mapToDepartureDto(TrekDeparture d) {
        return DepartureDto.builder()
                .id(d.getId())
                .trekListingId(d.getTrekListing().getId())
                .trekTitle(d.getTrekListing().getTitle())
                .destination(d.getTrekListing().getDestination())
                .organizerName(d.getTrekListing().getOrganizer().getOrganizerName())
                .startDate(d.getStartDate())
                .endDate(d.getEndDate())
                .price(d.getPrice())
                .capacity(d.getCapacity())
                .availableSlots(d.getAvailableSlots())
                .departureStatus(d.getDepartureStatus())
                .build();
    }
}
