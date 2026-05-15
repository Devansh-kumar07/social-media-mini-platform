package com.connectsphere.social.client;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * This class handles all HTTP calls to the search-service.
 *
 * When a post is created/updated/deleted, we need to tell search-service
 * so it can keep its index up-to-date. Same for users.
 *
 * Notice: every method has try-catch. This means if search-service is down,
 * the main operation (e.g., creating a post) still succeeds — we just log the error.
 */
@Component
public class SearchServiceClient {

    // Logger prints helpful messages to the console
    private static final Logger log = LoggerFactory.getLogger(SearchServiceClient.class);

    private final RestTemplate restTemplate;

    // This URL comes from application.yml. Default is "http://search-service" (Eureka service name)
    @Value("${services.search-url:http://localhost:8084}")
    private String searchServiceUrl;

    public SearchServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Tell search-service to index this post.
     * Called when a post is created OR updated.
     */
    public void indexPost(Long postId, Long authorId, String content) {
        try {
            String url = searchServiceUrl + "/api/v1/search/index/posts";

            // Build the request body (same fields as IndexPostRequest in search-service)
            Map<String, Object> body = new HashMap<>();
            body.put("postId", postId);
            body.put("authorId", authorId);
            body.put("content", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(url, request, Object.class);
            log.info("✅ Post {} successfully indexed in search-service", postId);

        } catch (Exception e) {
            // Don't crash the main operation just because search failed
            log.error("❌ Could not index post {} in search-service. Reason: {}", postId, e.getMessage());
        }
    }

    /**
     * Tell search-service to remove this post from the index.
     * Called when a post is deleted.
     */
    public void removePostFromIndex(Long postId) {
        try {
            String url = searchServiceUrl + "/api/v1/search/index/posts/" + postId + "/remove";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.postForObject(url, request, Object.class);
            log.info("✅ Post {} successfully removed from search index", postId);

        } catch (Exception e) {
            log.error("❌ Could not remove post {} from search index. Reason: {}", postId, e.getMessage());
        }
    }

    /**
     * Tell search-service to index this user.
     * Called when a new user registers, so others can find them.
     */
    public void indexUser(Long userId, String username, String fullName) {
        try {
            String url = searchServiceUrl + "/api/v1/search/index/users";

            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("username", username);
            body.put("fullName", fullName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(url, request, Object.class);
            log.info("✅ User {} successfully indexed in search-service", userId);

        } catch (Exception e) {
            log.error("❌ Could not index user {} in search-service. Reason: {}", userId, e.getMessage());
        }
    }
}
