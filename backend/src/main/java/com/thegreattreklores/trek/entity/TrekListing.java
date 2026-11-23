package com.thegreattreklores.trek.entity;

import com.thegreattreklores.common.entity.BaseEntity;
import com.thegreattreklores.organizer.entity.Organizer;
import com.thegreattreklores.trek.enums.ListingStatus;
import com.thegreattreklores.trek.enums.TrekDifficulty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trek_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrekListing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TrekDifficulty difficulty = TrekDifficulty.MODERATE;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "starting_price", precision = 10, scale = 2)
    private BigDecimal startingPrice;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_status", nullable = false, length = 30)
    @Builder.Default
    private ListingStatus listingStatus = ListingStatus.PUBLISHED;

    @OneToMany(mappedBy = "trekListing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TrekDeparture> departures = new ArrayList<>();
}
