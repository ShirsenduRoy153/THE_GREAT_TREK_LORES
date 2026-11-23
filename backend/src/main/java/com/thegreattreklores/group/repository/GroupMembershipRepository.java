package com.thegreattreklores.group.repository;

import com.thegreattreklores.group.entity.GroupMembership;
import com.thegreattreklores.group.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {

    List<GroupMembership> findByGroupId(UUID groupId);

    List<GroupMembership> findByGroupIdAndStatus(UUID groupId, MembershipStatus status);

    Optional<GroupMembership> findByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupMembership> findByUserIdAndStatus(UUID userId, MembershipStatus status);

    int countByGroupIdAndStatus(UUID groupId, MembershipStatus status);

    boolean existsByGroupIdAndUserIdAndStatus(UUID groupId, UUID userId, MembershipStatus status);
}
