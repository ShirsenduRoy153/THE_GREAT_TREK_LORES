package com.thegreattreklores.admin.controller;

import com.thegreattreklores.admin.dto.AdminDtos.*;
import com.thegreattreklores.admin.service.ReportService;
import com.thegreattreklores.auth.dto.UserPrincipal;
import com.thegreattreklores.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moderation & Safety", description = "Endpoints for submitting and reviewing content abuse reports")
public class AdminReportController {

    private final ReportService reportService;

    @PostMapping("/api/reports")
    @Operation(summary = "Submit abuse report", description = "Allows users to report inappropriate profiles, groups, or messages")
    public ResponseEntity<ApiResponse<ReportDto>> submitReport(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SubmitReportRequest request) {
        ReportDto response = reportService.submitReport(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report submitted for review", response));
    }

    @GetMapping("/api/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get open reports", description = "Admin only endpoint to review open moderation reports")
    public ResponseEntity<ApiResponse<List<ReportDto>>> getOpenReports(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ReportDto> response = reportService.getOpenReports(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/admin/reports/{reportId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve or reject report", description = "Admin resolves report and records audit trail")
    public ResponseEntity<ApiResponse<ReportDto>> resolveReport(
            @PathVariable UUID reportId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ResolveReportRequest request) {
        ReportDto response = reportService.resolveReport(reportId, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Report resolved", response));
    }
}
