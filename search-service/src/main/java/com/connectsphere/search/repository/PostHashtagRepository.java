package com.connectsphere.search.repository;

import com.connectsphere.search.model.PostHashtag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {
    List<PostHashtag> findByPostId(Long postId);

    List<PostHashtag> findByTagOrderByCreatedAtDesc(String tag);

    void deleteByPostId(Long postId);

    long countByTag(String tag);
}
