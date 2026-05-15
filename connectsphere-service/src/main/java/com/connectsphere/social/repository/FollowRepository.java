package com.connectsphere.social.repository;

import com.connectsphere.social.model.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    List<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId);

    List<Follow> findByFolloweeIdOrderByCreatedAtDesc(Long followeeId);

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    long countByFollowerId(Long followerId);

    long countByFolloweeId(Long followeeId);
}
