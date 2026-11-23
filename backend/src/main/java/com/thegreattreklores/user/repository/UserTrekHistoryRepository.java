package com.thegreattreklores.user.repository;

import com.thegreattreklores.user.entity.UserTrekHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserTrekHistoryRepository extends JpaRepository<UserTrekHistory, UUID> {
    List<UserTrekHistory> findByUserIdOrderByStartDateDesc(UUID userId);
}
