package com.thegreattreklores.group.repository;

import com.thegreattreklores.group.entity.TripGroup;
import com.thegreattreklores.group.enums.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripGroupRepository extends JpaRepository<TripGroup, UUID> {

    List<TripGroup> findByStatusOrderByCreatedAtDesc(GroupStatus status);

    List<TripGroup> findByDestinationContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(String destination, GroupStatus status);

    @Query("SELECT g FROM TripGroup g WHERE g.status = :status " +
           "AND (:destination IS NULL OR LOWER(g.destination) LIKE LOWER(CONCAT('%', :destination, '%'))) " +
           "AND (:startDate IS NULL OR g.preferredEndDate >= :startDate) " +
           "AND (:endDate IS NULL OR g.preferredStartDate <= :endDate) " +
           "ORDER BY g.createdAt DESC")
    List<TripGroup> searchGroups(
            @Param("status") GroupStatus status,
            @Param("destination") String destination,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
