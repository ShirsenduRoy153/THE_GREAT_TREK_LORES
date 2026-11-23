package com.thegreattreklores.chat.repository;

import com.thegreattreklores.chat.entity.ChatRoom;
import com.thegreattreklores.chat.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findByGroupIdAndRoomType(UUID groupId, RoomType roomType);

    Optional<ChatRoom> findByConnectionId(UUID connectionId);

    List<ChatRoom> findByGroupId(UUID groupId);

    @Query("SELECT DISTINCT r FROM ChatRoom r JOIN r.participants p " +
           "WHERE p.user.id = :userId AND p.status = 'ACTIVE' AND r.status = 'ACTIVE' " +
           "ORDER BY r.createdAt DESC")
    List<ChatRoom> findUserActiveRooms(@Param("userId") UUID userId);
}
