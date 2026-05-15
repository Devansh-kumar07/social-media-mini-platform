package com.connectsphere.social.service;

import com.connectsphere.social.dto.CommentResponse;
import com.connectsphere.social.dto.CreateCommentRequest;
import com.connectsphere.social.dto.UpdateCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse addComment(Long userId, Long postId, CreateCommentRequest request);

    CommentResponse getComment(Long commentId);

    Page<CommentResponse> getCommentsByUser(Long authorId, Pageable pageable);

    java.util.List<CommentResponse> getCommentsByPost(Long postId);

    CommentResponse updateComment(Long userId, Long commentId, UpdateCommentRequest request);

    void deleteComment(Long userId, Long commentId);

    void adminDeleteComment(Long commentId, Long adminUserId);
}
