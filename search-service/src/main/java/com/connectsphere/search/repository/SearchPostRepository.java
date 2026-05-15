package com.connectsphere.search.repository;

import com.connectsphere.search.model.SearchPost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchPostRepository extends JpaRepository<SearchPost, Long> {
    List<SearchPost> findByContentContainingIgnoreCaseOrderByIndexedAtDesc(String query);
}
