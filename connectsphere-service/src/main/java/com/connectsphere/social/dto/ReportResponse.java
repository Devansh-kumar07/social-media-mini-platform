package com.connectsphere.social.dto;

import com.connectsphere.social.model.ContentReport;
import com.connectsphere.social.model.ReportStatus;
import com.connectsphere.social.model.ReportTargetType;
import java.time.Instant;

public record ReportResponse(
        Long reportId,
        Long reporterId,
        Long targetId,
        ReportTargetType targetType,
        String reason,
        String details,
        ReportStatus status,
        Long resolvedByAdminId,
        Instant resolvedAt,
        Instant createdAt
) {
    public static ReportResponse from(ContentReport report) {
        return new ReportResponse(
                report.getReportId(),
                report.getReporterId(),
                report.getTargetId(),
                report.getTargetType(),
                report.getReason(),
                report.getDetails(),
                report.getStatus(),
                report.getResolvedByAdminId(),
                report.getResolvedAt(),
                report.getCreatedAt()
        );
    }
}
