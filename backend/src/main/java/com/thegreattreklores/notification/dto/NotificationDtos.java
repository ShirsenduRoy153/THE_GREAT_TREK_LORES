package com.thegreattreklores.notification.dto;

import com.thegreattreklores.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

public class NotificationDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDto {
        private UUID id;
        private NotificationType notificationType;
        private String title;
        private String body;
        private String relatedEntityType;
        private UUID relatedEntityId;
        private boolean isRead;
        private Instant createdAt;
    }
}
