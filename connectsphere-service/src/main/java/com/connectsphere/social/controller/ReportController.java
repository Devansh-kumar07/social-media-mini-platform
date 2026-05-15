package com.connectsphere.social.controller;

import com.connectsphere.social.dto.CreateReportRequest;
import com.connectsphere.social.dto.ReportResponse;
import com.connectsphere.social.dto.ResolveReportRequest;
import com.connectsphere.social.service.AdminModerationService;
import com.connectsphere.social.service.CommentService;
import com.connectsphere.social.service.PostService;
import com.connectsphere.social.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;
    private final PostService postService;
    private final CommentService commentService;
    private final AdminModerationService adminModerationService;

    public ReportController(
            ReportService reportService,
            PostService postService,
            CommentService commentService,
            AdminModerationService adminModerationService
    ) {
        this.reportService = reportService;
        this.postService = postService;
        this.commentService = commentService;
        this.adminModerationService = adminModerationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a report")
    public ReportResponse createReport(@Valid @RequestBody CreateReportRequest request) {
        return reportService.createReport(request);
    }

    @GetMapping("/open")
    @Operation(summary = "Get open reports", security = @SecurityRequirement(name = "x-user-id"))
    public List<ReportResponse> getOpenReports() {
        return reportService.getOpenReports();
    }

    @PutMapping("/{reportId}/resolve")
    @Operation(summary = "Resolve a report", security = @SecurityRequirement(name = "x-user-id"))
    public ReportResponse resolveReport(
            @PathVariable("reportId") Long reportId,
            @Valid @RequestBody ResolveReportRequest request
    ) {
        return reportService.resolveReport(reportId, request);
    }

    @DeleteMapping("/admin/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Admin remove a post", security = @SecurityRequirement(name = "x-user-id"))
    public void adminDeletePost(
            @org.springframework.web.bind.annotation.RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("postId") Long postId
    ) {
        adminModerationService.ensureAdmin(userId);
        postService.adminDeletePost(postId, userId);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Admin remove a comment", security = @SecurityRequirement(name = "x-user-id"))
    public void adminDeleteComment(
            @org.springframework.web.bind.annotation.RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("commentId") Long commentId
    ) {
        adminModerationService.ensureAdmin(userId);
        commentService.adminDeleteComment(commentId, userId);
    }
}
