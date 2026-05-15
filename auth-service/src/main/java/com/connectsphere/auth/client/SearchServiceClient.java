package com.connectsphere.auth.client;

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
 * This client lets auth-service talk to search-service.
 *
 * When a new user registers, we need to index them in search-service
 * so other users can find them by username or name.
 */
@Component
public class SearchServiceClient {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.search-url:http://localhost:8084}")
    private String searchServiceUrl;

    public SearchServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Index a newly registered user in the search-service.
     * After this, other users can search for them by username or full name.
     */
    public void indexUser(Long userId, String username, String fullName) {
        try {
            String url = searchServiceUrl + "/api/v1/search/index/users";

            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("username", username);
            body.put("fullName", fullName != null ? fullName : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(url, request, Object.class);
            log.info("✅ New user {} indexed in search-service", userId);

        } catch (Exception e) {
            // Registration still succeeds even if search-service is unavailable
            log.error("❌ Could not index user {} in search-service. Reason: {}", userId, e.getMessage());
        }
    }
}
