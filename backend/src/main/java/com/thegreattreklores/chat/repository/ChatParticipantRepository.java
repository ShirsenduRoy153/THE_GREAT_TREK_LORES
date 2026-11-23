package com.thegreattreklores.chat.repository;

import com.thegreattreklores.chat.entity.ChatParticipant;
import com.thegreattreklores.chat.enums.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    Optional<ChatParticipant> findByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);

    boolean existsByChatRoomIdAndUserIdAndStatus(UUID chatRoomId, UUID userId, ParticipantStatus status);

    List<ChatParticipant> findByChatRoomIdAndStatus(UUID chatRoomId, ParticipantStatus status);

    List<ChatParticipant> findByUserIdAndStatus(UUID userId, ParticipantStatus status);
}
