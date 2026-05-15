package com.connectsphere.search.repository;

import com.connectsphere.search.model.HashtagIndex;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagIndexRepository extends JpaRepository<HashtagIndex, String> {
    List<HashtagIndex> findByTagContainingIgnoreCaseOrderByUsageCountDesc(String query);

    List<HashtagIndex> findTop10ByOrderByUsageCountDesc();
}
