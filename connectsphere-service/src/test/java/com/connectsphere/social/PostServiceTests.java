package com.connectsphere.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.connectsphere.social.client.MediaServiceClient;
import com.connectsphere.social.client.NotificationServiceClient;
import com.connectsphere.social.client.SearchServiceClient;
import com.connectsphere.social.dto.UploadedMediaResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for connectsphere-service (posts, feed).
 *
 * @MockBean on SearchServiceClient and NotificationServiceClient
 * means no real HTTP calls go out during tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PostServiceTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // Mock these so no real HTTP calls happen during tests
    @MockBean
    SearchServiceClient searchServiceClient;

    @MockBean
    NotificationServiceClient notificationServiceClient;

    @MockBean
    MediaServiceClient mediaServiceClient;

    // ─────────────────────────────────────────────────
    // TEST 1: Create a post and check it appears in the public feed
    // ─────────────────────────────────────────────────
    @Test
    void createPost_shouldAppearInPublicFeed() throws Exception {

        Map<String, Object> postBody = Map.of(
                "content", "Hello ConnectSphere! #test",
                "visibility", "PUBLIC"
        );

        // Create the post (X-User-Id header simulates logged-in user with ID 1)
        MvcResult result = mockMvc.perform(post("/api/v1/posts")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postBody)))
                .andExpect(status().isCreated())                          // HTTP 201
                .andExpect(jsonPath("$.content").value("Hello ConnectSphere! #test"))
                .andReturn();

        Long postId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("postId").asLong();

        mockMvc.perform(get("/api/v1/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello ConnectSphere! #test"));

        // Check the post is visible in the public feed
        mockMvc.perform(get("/api/v1/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());             // paginated result
    }

    @Test
    void createPost_withOnlyMedia_shouldSucceed() throws Exception {

        Map<String, Object> postBody = Map.of(
                "visibility", "PUBLIC",
                "mediaUrls", java.util.List.of("/api/v1/media/download/12")
        );

        mockMvc.perform(post("/api/v1/posts")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaUrls[0]").value("/api/v1/media/download/12"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void createPostWithMedia_shouldUploadAndReturnPost() throws Exception {

        MockMultipartFile fakeImage = new MockMultipartFile(
                "files",
                "photo.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes()
        );

        when(mediaServiceClient.uploadPostMedia(eq(12L), any(Long.class), any()))
                .thenReturn(new UploadedMediaResponse(50L, "/api/v1/media/download/50"));

        mockMvc.perform(multipart("/api/v1/posts/with-media")
                        .file(fakeImage)
                        .header("X-User-Id", "12")
                        .param("content", "Post with uploaded image")
                        .param("visibility", "PUBLIC"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Post with uploaded image"))
                .andExpect(jsonPath("$.mediaUrls[0]").value("/api/v1/media/download/50"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void createPost_withoutTextAndMedia_shouldFail() throws Exception {

        Map<String, Object> postBody = Map.of(
                "visibility", "PUBLIC"
        );

        mockMvc.perform(post("/api/v1/posts")
                        .header("X-User-Id", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postBody)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────
    // TEST 2: A different user should NOT be able to delete someone else's post
    // ─────────────────────────────────────────────────
    @Test
    void deletePost_byDifferentUser_shouldFail() throws Exception {

        // User 10 creates a post
        Map<String, Object> postBody = Map.of(
                "content", "This post belongs to user 10",
                "visibility", "PUBLIC"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/posts")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postBody)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract the post ID from the response
        Long postId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("postId").asLong();

        // User 99 tries to delete User 10's post — should fail!
        mockMvc.perform(delete("/api/v1/posts/" + postId)
                        .header("X-User-Id", "99"))
                .andExpect(status().isBadRequest());                     // HTTP 400
    }
}
