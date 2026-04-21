package com.connectsphere.social.repository;

import com.connectsphere.social.model.Post;
import com.connectsphere.social.model.PostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByDeletedFalseAndVisibility(PostVisibility visibility, Pageable pageable);

    Page<Post> findByDeletedFalseAndAuthorId(Long authorId, Pageable pageable);

    Page<Post> findByDeletedFalseAndContentContainingIgnoreCase(String query, Pageable pageable);
}

