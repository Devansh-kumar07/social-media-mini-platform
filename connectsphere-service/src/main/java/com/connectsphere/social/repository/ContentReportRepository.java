package com.connectsphere.social.repository;

import com.connectsphere.social.model.ContentReport;
import com.connectsphere.social.model.ReportStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {
    List<ContentReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);
}
