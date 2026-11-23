package com.thegreattreklores.trek.repository;

import com.thegreattreklores.trek.entity.TrekDeparture;
import com.thegreattreklores.trek.enums.DepartureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrekDepartureRepository extends JpaRepository<TrekDeparture, UUID> {

    List<TrekDeparture> findByTrekListingIdOrderByStartDateAsc(UUID trekListingId);

    List<TrekDeparture> findByTrekListingIdAndDepartureStatusOrderByStartDateAsc(UUID trekListingId, DepartureStatus status);

    @Query("SELECT d FROM TrekDeparture d WHERE d.departureStatus = 'OPEN' AND d.availableSlots > 0 " +
           "AND (:destination IS NULL OR LOWER(d.trekListing.destination) LIKE LOWER(CONCAT('%', :destination, '%'))) " +
           "AND (d.startDate <= :groupEndDate AND d.endDate >= :groupStartDate) " +
           "ORDER BY d.startDate ASC")
    List<TrekDeparture> findOverlappingDepartures(
            @Param("destination") String destination,
            @Param("groupStartDate") LocalDate groupStartDate,
            @Param("groupEndDate") LocalDate groupEndDate);
}
