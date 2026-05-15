package com.connectsphere.social.service;

import com.connectsphere.social.dto.CreateReportRequest;
import com.connectsphere.social.dto.ReportResponse;
import com.connectsphere.social.dto.ResolveReportRequest;
import java.util.List;

public interface ReportService {
    ReportResponse createReport(CreateReportRequest request);

    List<ReportResponse> getOpenReports();

    ReportResponse resolveReport(Long reportId, ResolveReportRequest request);
}
