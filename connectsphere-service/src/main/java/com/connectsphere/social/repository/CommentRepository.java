package com.connectsphere.social.repository;

import com.connectsphere.social.model.Comment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(Long postId);

    List<Comment> findByParentCommentIdAndDeletedFalseOrderByCreatedAtAsc(Long parentCommentId);

    Page<Comment> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    long countByPostIdAndDeletedFalse(Long postId);
}
