package com.thegreattreklores.trek.repository;

import com.thegreattreklores.trek.entity.TrekListing;
import com.thegreattreklores.trek.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrekListingRepository extends JpaRepository<TrekListing, UUID> {

    List<TrekListing> findByListingStatusOrderByCreatedAtDesc(ListingStatus status);

    List<TrekListing> findByOrganizerIdOrderByCreatedAtDesc(UUID organizerId);

    List<TrekListing> findByDestinationContainingIgnoreCaseAndListingStatusOrderByCreatedAtDesc(String destination, ListingStatus status);
}
