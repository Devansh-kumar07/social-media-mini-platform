package com.connectsphere.search.controller;

import com.connectsphere.search.dto.HashtagResponse;
import com.connectsphere.search.dto.IndexPostRequest;
import com.connectsphere.search.dto.IndexUserRequest;
import com.connectsphere.search.dto.SearchPostResponse;
import com.connectsphere.search.dto.SearchUserResponse;
import com.connectsphere.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/health")
    @Operation(summary = "Check search service health")
    public String health() {
        return "search-service is running";
    }

    @PostMapping("/index/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Index or update a post for search")
    public SearchPostResponse indexPost(@Valid @RequestBody IndexPostRequest request) {
        return searchService.indexPost(request);
    }

    @PostMapping("/index/posts/{postId}/remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a post from search index")
    public void removePostIndex(@PathVariable("postId") Long postId) {
        searchService.removePostIndex(postId);
    }

    @PostMapping("/index/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Index or update a user for search")
    public SearchUserResponse indexUser(@Valid @RequestBody IndexUserRequest request) {
        return searchService.indexUser(request);
    }

    @GetMapping("/posts")
    @Operation(summary = "Search indexed posts")
    public List<SearchPostResponse> searchPosts(@RequestParam(name = "q", defaultValue = "") String query) {
        return searchService.searchPosts(query);
    }

    @GetMapping("/users")
    @Operation(summary = "Search indexed users")
    public List<SearchUserResponse> searchUsers(@RequestParam(name = "q", defaultValue = "") String query) {
        return searchService.searchUsers(query);
    }

    @GetMapping("/hashtags")
    @Operation(summary = "Search hashtags")
    public List<HashtagResponse> searchHashtags(@RequestParam(name = "q", defaultValue = "") String query) {
        return searchService.searchHashtags(query);
    }

    @GetMapping("/hashtags/trending")
    @Operation(summary = "Get trending hashtags")
    public List<HashtagResponse> trendingHashtags(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        return searchService.trendingHashtags(limit);
    }

    @GetMapping("/hashtags/post/{postId}")
    @Operation(summary = "Get hashtags for a post")
    public List<String> getHashtagsForPost(@PathVariable("postId") Long postId) {
        return searchService.getHashtagsForPost(postId);
    }

    @GetMapping("/hashtags/{tag}/posts")
    @Operation(summary = "Get posts by hashtag")
    public List<SearchPostResponse> getPostsByHashtag(@PathVariable("tag") String tag) {
        return searchService.getPostsByHashtag(tag);
    }

    @GetMapping("/hashtags/{tag}/count")
    @Operation(summary = "Get hashtag post count")
    public long getHashtagCount(@PathVariable("tag") String tag) {
        return searchService.getHashtagCount(tag);
    }
}
