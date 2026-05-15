package com.connectsphere.social.controller;

import com.connectsphere.social.dto.CommentResponse;
import com.connectsphere.social.dto.CreateCommentRequest;
import com.connectsphere.social.dto.UpdateCommentRequest;
import com.connectsphere.social.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/post/{postId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment or reply", security = @SecurityRequirement(name = "x-user-id"))
    public CommentResponse addComment(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return commentService.addComment(userId, postId, request);
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Get comment by id")
    public CommentResponse getComment(@PathVariable("commentId") Long commentId) {
        return commentService.getComment(commentId);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get comments by post")
    public List<CommentResponse> getCommentsByPost(@PathVariable("postId") Long postId) {
        return commentService.getCommentsByPost(postId);
    }

    @GetMapping("/user/{authorId}")
    @Operation(summary = "Get comments by user")
    public Page<CommentResponse> getCommentsByUser(@PathVariable("authorId") Long authorId, Pageable pageable) {
        return commentService.getCommentsByUser(authorId, pageable);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update own comment", security = @SecurityRequirement(name = "x-user-id"))
    public CommentResponse updateComment(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        return commentService.updateComment(userId, commentId, request);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete own comment", security = @SecurityRequirement(name = "x-user-id"))
    public void deleteComment(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("commentId") Long commentId
    ) {
        commentService.deleteComment(userId, commentId);
    }
}
