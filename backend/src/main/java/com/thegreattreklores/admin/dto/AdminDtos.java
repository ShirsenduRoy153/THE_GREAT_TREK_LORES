package com.thegreattreklores.admin.dto;

import com.thegreattreklores.admin.enums.ReportStatus;
import com.thegreattreklores.admin.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

public class AdminDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitReportRequest {
        @NotNull(message = "Target type is required")
        private ReportTargetType targetType;

        @NotNull(message = "Target ID is required")
        private UUID targetId;

        @NotBlank(message = "Reason is required")
        private String reason;

        private String details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportDto {
        private UUID id;
        private UUID reportedByUserId;
        private String reportedByEmail;
        private ReportTargetType targetType;
        private UUID targetId;
        private String reason;
        private String details;
        private ReportStatus status;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolveReportRequest {
        @NotNull
        private ReportStatus status; // RESOLVED or REJECTED
        private String adminNotes;
    }
}
