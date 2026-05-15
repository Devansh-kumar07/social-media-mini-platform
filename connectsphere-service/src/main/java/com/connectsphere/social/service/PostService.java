package com.connectsphere.social.service;

import com.connectsphere.social.dto.CreatePostRequest;
import com.connectsphere.social.dto.PostResponse;
import com.connectsphere.social.dto.SocialStatsResponse;
import com.connectsphere.social.dto.UpdatePostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
    PostResponse createPost(Long authorId, CreatePostRequest request);

    PostResponse createPostWithMedia(Long authorId, String content, String visibility, MultipartFile[] files);

    PostResponse getPost(Long postId);

    Page<PostResponse> getPublicFeed(Pageable pageable);

    Page<PostResponse> getPostsByUser(Long authorId, Pageable pageable);

    Page<PostResponse> getFeedForUser(Long userId, Pageable pageable);

    Page<PostResponse> searchPosts(String query, Pageable pageable);

    PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request);

    PostResponse changeVisibility(Long postId, Long userId, UpdatePostRequest request);

    void deletePost(Long postId, Long userId);

    void adminDeletePost(Long postId, Long adminUserId);

    long getPostCount(Long authorId);

    SocialStatsResponse getStats();
}
