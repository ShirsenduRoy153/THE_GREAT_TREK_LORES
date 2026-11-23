package com.thegreattreklores.connection.repository;

import com.thegreattreklores.connection.entity.GroupOrganizerConnection;
import com.thegreattreklores.connection.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupOrganizerConnectionRepository extends JpaRepository<GroupOrganizerConnection, UUID> {

    List<GroupOrganizerConnection> findByGroupIdOrderByCreatedAtDesc(UUID groupId);

    List<GroupOrganizerConnection> findByOrganizerIdOrderByCreatedAtDesc(UUID organizerId);

    Optional<GroupOrganizerConnection> findByGroupIdAndOrganizerId(UUID groupId, UUID organizerId);

    boolean existsByGroupIdAndOrganizerIdAndStatusIn(UUID groupId, UUID organizerId, List<ConnectionStatus> statuses);
}
