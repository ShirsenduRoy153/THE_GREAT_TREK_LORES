package com.thegreattreklores.trek.entity;

import com.thegreattreklores.common.entity.BaseEntity;
import com.thegreattreklores.trek.enums.DepartureStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trek_departures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrekDeparture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trek_listing_id", nullable = false)
    private TrekListing trekListing;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Enumerated(EnumType.STRING)
    @Column(name = "departure_status", nullable = false, length = 30)
    @Builder.Default
    private DepartureStatus departureStatus = DepartureStatus.OPEN;
}
