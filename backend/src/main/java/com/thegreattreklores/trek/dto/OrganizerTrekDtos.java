package com.thegreattreklores.trek.dto;

import com.thegreattreklores.organizer.enums.VerificationStatus;
import com.thegreattreklores.trek.enums.DepartureStatus;
import com.thegreattreklores.trek.enums.ListingStatus;
import com.thegreattreklores.trek.enums.TrekDifficulty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class OrganizerTrekDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrganizerRequest {
        @NotBlank(message = "Organizer name is required")
        private String organizerName;
        private String description;
        private String logoUrl;
        private String location;
        private String websiteUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizerDto {
        private UUID id;
        private UUID ownerUserId;
        private String organizerName;
        private String description;
        private String logoUrl;
        private String location;
        private String websiteUrl;
        private VerificationStatus verificationStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTrekListingRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Destination is required")
        private String destination;

        private String description;

        @Builder.Default
        private TrekDifficulty difficulty = TrekDifficulty.MODERATE;

        @NotNull(message = "Duration in days is required")
        @Min(value = 1, message = "Duration must be at least 1 day")
        private Integer durationDays;

        @DecimalMin(value = "0.0", message = "Starting price must be positive")
        private BigDecimal startingPrice;

        private String coverImageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrekListingSummaryDto {
        private UUID id;
        private UUID organizerId;
        private String organizerName;
        private String title;
        private String destination;
        private String description;
        private TrekDifficulty difficulty;
        private Integer durationDays;
        private BigDecimal startingPrice;
        private String coverImageUrl;
        private ListingStatus listingStatus;
        private Integer upcomingDepartureCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrekListingDetailDto {
        private UUID id;
        private OrganizerDto organizer;
        private String title;
        private String destination;
        private String description;
        private TrekDifficulty difficulty;
        private Integer durationDays;
        private BigDecimal startingPrice;
        private String coverImageUrl;
        private ListingStatus listingStatus;
        private List<DepartureDto> departures;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDepartureRequest {
        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price cannot be negative")
        private BigDecimal price;

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        private Integer capacity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartureDto {
        private UUID id;
        private UUID trekListingId;
        private String trekTitle;
        private String destination;
        private String organizerName;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal price;
        private Integer capacity;
        private Integer availableSlots;
        private DepartureStatus departureStatus;
    }
}
