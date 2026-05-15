package com.connectsphere.social.dto;

public record SocialStatsResponse(
        long totalPosts,
        long totalComments,
        long totalReactions,
        long openReports
) {
}
