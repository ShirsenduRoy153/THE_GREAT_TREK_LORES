package com.thegreattreklores.admin.service;

import com.thegreattreklores.admin.dto.AdminDtos.*;
import com.thegreattreklores.admin.entity.AdminAction;
import com.thegreattreklores.admin.entity.Report;
import com.thegreattreklores.admin.enums.AdminActionType;
import com.thegreattreklores.admin.enums.ReportStatus;
import com.thegreattreklores.admin.repository.AdminActionRepository;
import com.thegreattreklores.admin.repository.ReportRepository;
import com.thegreattreklores.user.entity.User;
import com.thegreattreklores.user.enums.UserRole;
import com.thegreattreklores.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final AdminActionRepository adminActionRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportDto submitReport(UUID reporterUserId, SubmitReportRequest request) {
        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Report report = Report.builder()
                .reportedByUser(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason().trim())
                .details(request.getDetails())
                .status(ReportStatus.OPEN)
                .build();

        report = reportRepository.save(report);

        return mapToDto(report);
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getOpenReports(UUID adminUserId) {
        verifyAdmin(adminUserId);
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportDto resolveReport(UUID reportId, UUID adminUserId, ResolveReportRequest request) {
        User admin = verifyAdmin(adminUserId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setStatus(request.getStatus());
        report.setReviewedByAdminUser(admin);
        report = reportRepository.save(report);

        AdminAction action = AdminAction.builder()
                .adminUser(admin)
                .actionType(request.getStatus() == ReportStatus.RESOLVED ? AdminActionType.RESOLVE_REPORT : AdminActionType.REJECT_REPORT)
                .targetType("REPORT")
                .targetId(reportId)
                .reason(request.getAdminNotes())
                .build();

        adminActionRepository.save(action);
        return mapToDto(report);
    }

    private User verifyAdmin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Access denied: Requires platform administrator role");
        }
        return user;
    }

    private ReportDto mapToDto(Report r) {
        return ReportDto.builder()
                .id(r.getId())
                .reportedByUserId(r.getReportedByUser().getId())
                .reportedByEmail(r.getReportedByUser().getEmail())
                .targetType(r.getTargetType())
                .targetId(r.getTargetId())
                .reason(r.getReason())
                .details(r.getDetails())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
