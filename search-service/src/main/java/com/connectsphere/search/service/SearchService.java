package com.connectsphere.search.service;

import com.connectsphere.search.dto.HashtagResponse;
import com.connectsphere.search.dto.IndexPostRequest;
import com.connectsphere.search.dto.IndexUserRequest;
import com.connectsphere.search.dto.SearchPostResponse;
import com.connectsphere.search.dto.SearchUserResponse;
import java.util.List;

public interface SearchService {
    SearchPostResponse indexPost(IndexPostRequest request);

    void removePostIndex(Long postId);

    SearchUserResponse indexUser(IndexUserRequest request);

    List<SearchPostResponse> searchPosts(String query);

    List<SearchUserResponse> searchUsers(String query);

    List<HashtagResponse> searchHashtags(String query);

    List<HashtagResponse> trendingHashtags(int limit);

    List<String> getHashtagsForPost(Long postId);

    List<SearchPostResponse> getPostsByHashtag(String tag);

    long getHashtagCount(String tag);
}
