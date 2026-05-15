package com.connectsphere.social.service;

import com.connectsphere.social.dto.CreateReportRequest;
import com.connectsphere.social.dto.ReportResponse;
import com.connectsphere.social.dto.ResolveReportRequest;
import com.connectsphere.social.model.Comment;
import com.connectsphere.social.model.ContentReport;
import com.connectsphere.social.model.Post;
import com.connectsphere.social.model.ReportStatus;
import com.connectsphere.social.model.ReportTargetType;
import com.connectsphere.social.repository.CommentRepository;
import com.connectsphere.social.repository.ContentReportRepository;
import com.connectsphere.social.repository.PostRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportServiceImpl implements ReportService {
    private final ContentReportRepository contentReportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public ReportServiceImpl(
            ContentReportRepository contentReportRepository,
            PostRepository postRepository,
            CommentRepository commentRepository
    ) {
        this.contentReportRepository = contentReportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        validateTarget(request.targetId(), request.targetType());

        ContentReport report = new ContentReport();
        report.setReporterId(request.reporterId());
        report.setTargetId(request.targetId());
        report.setTargetType(request.targetType());
        report.setReason(request.reason());
        report.setDetails(request.details());

        return ReportResponse.from(contentReportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getOpenReports() {
        return contentReportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN).stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ReportResponse resolveReport(Long reportId, ResolveReportRequest request) {
        ContentReport report = contentReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.setStatus(request.status());
        report.setResolvedByAdminId(request.adminUserId());
        report.setResolvedAt(Instant.now());
        return ReportResponse.from(contentReportRepository.save(report));
    }

    private void validateTarget(Long targetId, ReportTargetType targetType) {
        if (targetType == ReportTargetType.POST) {
            Post post = postRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));
            if (post.isDeleted()) {
                throw new IllegalArgumentException("Post not found");
            }
            return;
        }

        if (targetType == ReportTargetType.COMMENT) {
            Comment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
            if (comment.isDeleted()) {
                throw new IllegalArgumentException("Comment not found");
            }
        }
    }
}
