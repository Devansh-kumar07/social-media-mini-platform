package com.connectsphere.media.repository;

import com.connectsphere.media.model.MediaFile;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<MediaFile, Long> {
    List<MediaFile> findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(Long ownerId);

    List<MediaFile> findByStoryTrueAndDeletedFalseAndExpiresAtAfterOrderByCreatedAtDesc(Instant now);

    List<MediaFile> findByStoryTrueAndOwnerIdAndDeletedFalseOrderByCreatedAtDesc(Long ownerId);

    List<MediaFile> findByLinkedPostIdAndDeletedFalseOrderByCreatedAtAsc(Long linkedPostId);

    List<MediaFile> findByStoryTrueAndDeletedFalseAndExpiresAtBefore(Instant now);
}
