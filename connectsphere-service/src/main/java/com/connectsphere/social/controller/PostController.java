package com.connectsphere.social.controller;

import com.connectsphere.social.dto.CreatePostRequest;
import com.connectsphere.social.dto.PostResponse;
import com.connectsphere.social.dto.SocialStatsResponse;
import com.connectsphere.social.dto.UpdatePostRequest;
import com.connectsphere.social.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/health")
    @Operation(summary = "Check post service health")
    public String health() {
        return "connectsphere-service is running";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new post", security = @SecurityRequirement(name = "x-user-id"))
    public PostResponse createPost(
            @RequestHeader(name = "X-User-Id") Long userId,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return postService.createPost(userId, request);
    }

    @PostMapping(value = "/with-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new post with uploaded media", security = @SecurityRequirement(name = "x-user-id"))
    public PostResponse createPostWithMedia(
            @RequestHeader(name = "X-User-Id") Long userId,
            @RequestParam(name = "content", required = false) String content,
            @RequestParam(name = "visibility", required = false) String visibility,
            @RequestParam(name = "files", required = false) MultipartFile[] files
    ) {
        return postService.createPostWithMedia(userId, content, visibility, files);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get post by id")
    public PostResponse getPost(@PathVariable("postId") Long postId) {
        return postService.getPost(postId);
    }

    @GetMapping("/feed")
    @Operation(summary = "Get public feed")
    public Page<PostResponse> publicFeed(Pageable pageable) {
        return postService.getPublicFeed(pageable);
    }

    @GetMapping("/feed/user/{userId}")
    @Operation(summary = "Get personalised feed by followed users")
    public Page<PostResponse> personalisedFeed(@PathVariable("userId") Long userId, Pageable pageable) {
        return postService.getFeedForUser(userId, pageable);
    }

    @GetMapping("/user/{authorId}")
    @Operation(summary = "Get posts by author")
    public Page<PostResponse> postsByUser(@PathVariable("authorId") Long authorId, Pageable pageable) {
        return postService.getPostsByUser(authorId, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts")
    public Page<PostResponse> searchPosts(@RequestParam(name = "q", defaultValue = "") String q, Pageable pageable) {
        return postService.searchPosts(q, pageable);
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update post", security = @SecurityRequirement(name = "x-user-id"))
    public PostResponse updatePost(
            @PathVariable("postId") Long postId,
            @RequestHeader(name = "X-User-Id") Long userId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return postService.updatePost(postId, userId, request);
    }

    @PutMapping("/{postId}/visibility")
    @Operation(summary = "Change post visibility", security = @SecurityRequirement(name = "x-user-id"))
    public PostResponse changeVisibility(
            @PathVariable("postId") Long postId,
            @RequestHeader(name = "X-User-Id") Long userId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return postService.changeVisibility(postId, userId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete post", security = @SecurityRequirement(name = "x-user-id"))
    public void deletePost(@PathVariable("postId") Long postId, @RequestHeader(name = "X-User-Id") Long userId) {
        postService.deletePost(postId, userId);
    }

    @GetMapping("/user/{authorId}/count")
    @Operation(summary = "Get post count by author")
    public long getPostCount(@PathVariable("authorId") Long authorId) {
        return postService.getPostCount(authorId);
    }

    @GetMapping("/admin/stats")
    @Operation(summary = "Get simple social stats")
    public SocialStatsResponse getStats() {
        return postService.getStats();
    }
}
