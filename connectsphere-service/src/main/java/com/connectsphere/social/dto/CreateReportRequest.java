package com.connectsphere.social.dto;

import com.connectsphere.social.model.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotNull Long reporterId,
        @NotNull Long targetId,
        @NotNull ReportTargetType targetType,
        @NotBlank @Size(max = 120) String reason,
        @Size(max = 500) String details
) {
}
