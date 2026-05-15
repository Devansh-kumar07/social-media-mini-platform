package com.connectsphere.search.dto;

import com.connectsphere.search.model.SearchUser;
import java.time.Instant;

public record SearchUserResponse(
        Long userId,
        String username,
        String fullName,
        Instant indexedAt
) {
    public static SearchUserResponse from(SearchUser searchUser) {
        return new SearchUserResponse(
                searchUser.getUserId(),
                searchUser.getUsername(),
                searchUser.getFullName(),
                searchUser.getIndexedAt()
        );
    }
}
