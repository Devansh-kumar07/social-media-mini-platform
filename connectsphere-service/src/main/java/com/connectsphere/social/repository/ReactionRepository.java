package com.connectsphere.social.repository;

import com.connectsphere.social.model.Reaction;
import com.connectsphere.social.model.ReactionTargetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, ReactionTargetType targetType);

    List<Reaction> findByTargetIdAndTargetType(Long targetId, ReactionTargetType targetType);

    List<Reaction> findByUserId(Long userId);
}
