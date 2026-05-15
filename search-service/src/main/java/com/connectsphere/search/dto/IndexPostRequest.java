package com.connectsphere.search.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IndexPostRequest(
        @NotNull Long postId,
        @NotNull Long authorId,
        @NotNull @Size(max = 2000) String content
) {
}
