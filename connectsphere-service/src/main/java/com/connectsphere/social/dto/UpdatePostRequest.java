package com.connectsphere.social.dto;

import com.connectsphere.social.model.PostVisibility;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @Size(max = 2000) String content,
        PostVisibility visibility
) {
}

