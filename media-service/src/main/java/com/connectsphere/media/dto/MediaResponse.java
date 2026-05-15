package com.connectsphere.media.dto;

import com.connectsphere.media.model.MediaFile;
import com.connectsphere.media.model.MediaKind;
import java.time.Instant;

public record MediaResponse(
        Long mediaId,
        Long ownerId,
        Long linkedPostId,
        String originalFileName,
        String contentType,
        long fileSize,
        MediaKind mediaKind,
        boolean story,
        String caption,
        long viewsCount,
        Instant expiresAt,
        String downloadUrl,
        Instant createdAt
) {
    public static MediaResponse from(MediaFile mediaFile) {
        return new MediaResponse(
                mediaFile.getMediaId(),
                mediaFile.getOwnerId(),
                mediaFile.getLinkedPostId(),
                mediaFile.getOriginalFileName(),
                mediaFile.getContentType(),
                mediaFile.getFileSize(),
                mediaFile.getMediaKind(),
                mediaFile.isStory(),
                mediaFile.getCaption(),
                mediaFile.getViewsCount(),
                mediaFile.getExpiresAt(),
                "/api/v1/media/download/" + mediaFile.getMediaId(),
                mediaFile.getCreatedAt()
        );
    }
}
