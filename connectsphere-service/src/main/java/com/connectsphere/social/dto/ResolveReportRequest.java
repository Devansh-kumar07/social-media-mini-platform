package com.connectsphere.social.dto;

import com.connectsphere.social.model.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record ResolveReportRequest(
        @NotNull Long adminUserId,
        @NotNull ReportStatus status
) {
}
