package com.thegreattreklores.user.repository;

import com.thegreattreklores.user.entity.UserSocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSocialLinkRepository extends JpaRepository<UserSocialLink, UUID> {
    List<UserSocialLink> findByUserId(UUID userId);
}
