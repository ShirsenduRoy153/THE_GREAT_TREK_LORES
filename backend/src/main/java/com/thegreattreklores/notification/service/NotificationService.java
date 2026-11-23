package com.thegreattreklores.notification.service;

import com.thegreattreklores.notification.dto.NotificationDtos.NotificationDto;
import com.thegreattreklores.notification.entity.Notification;
import com.thegreattreklores.notification.enums.NotificationType;
import com.thegreattreklores.notification.repository.NotificationRepository;
import com.thegreattreklores.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendNotification(
            User recipient,
            NotificationType type,
            String title,
            String body,
            String entityType,
            UUID entityId) {
        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .notificationType(type)
                .title(title)
                .body(body)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notification sent to {}: {}", recipient.getEmail(), title);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(UUID userId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> NotificationDto.builder()
                        .id(n.getId())
                        .notificationType(n.getNotificationType())
                        .title(n.getTitle())
                        .body(n.getBody())
                        .relatedEntityType(n.getRelatedEntityType())
                        .relatedEntityId(n.getRelatedEntityId())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getRecipientUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> list = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(list);
    }
}
