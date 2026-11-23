package com.thegreattreklores.connection.dto;

import com.thegreattreklores.connection.enums.ConnectionInitiator;
import com.thegreattreklores.connection.enums.ConnectionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ConnectionDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateConnectionRequest {
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateConnectionStatusRequest {
        @NotNull(message = "Status is required")
        private ConnectionStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionDto {
        private UUID id;
        private UUID groupId;
        private String groupName;
        private String groupDestination;
        private LocalDate groupStartDate;
        private LocalDate groupEndDate;
        private UUID organizerId;
        private String organizerName;
        private String organizerLocation;
        private ConnectionInitiator initiatedBy;
        private String message;
        private ConnectionStatus status;
        private Instant createdAt;
    }
}
