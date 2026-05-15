package com.connectsphere.social.dto;

public record UploadedMediaResponse(
        Long mediaId,
        String downloadUrl
) {
}
