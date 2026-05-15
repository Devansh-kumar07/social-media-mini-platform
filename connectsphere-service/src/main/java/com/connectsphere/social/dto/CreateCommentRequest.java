package com.connectsphere.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        Long parentCommentId,
        @NotBlank @Size(max = 1000) String content
) {
}
