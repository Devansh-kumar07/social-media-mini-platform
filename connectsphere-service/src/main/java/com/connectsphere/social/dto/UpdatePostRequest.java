package com.connectsphere.social.dto;

import com.connectsphere.social.model.PostVisibility;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdatePostRequest(
        @Size(max = 2000) String content,
        PostVisibility visibility,
        List<String> mediaUrls
) {
}
