package com.connectsphere.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for search-service.
 *
 * We test indexing posts and searching by keywords/hashtags.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SearchServiceTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────
    // TEST 1: Index a post with a hashtag, then search for it by hashtag
    // ─────────────────────────────────────────────────
    @Test
    void indexPostWithHashtag_shouldBeFoundByHashtagSearch() throws Exception {

        // Index a post that contains #springboot
        Map<String, Object> indexRequest = Map.of(
                "postId", 100L,
                "authorId", 1L,
                "content", "Learning #springboot today, very exciting!"
        );

        mockMvc.perform(post("/api/v1/search/index/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(indexRequest)))
                .andExpect(status().isCreated());                         // HTTP 201

        // Now search hashtags — should find #springboot
        mockMvc.perform(get("/api/v1/search/hashtags")
                        .param("q", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].tag").value("#springboot"));
    }

    // ─────────────────────────────────────────────────
    // TEST 2: Searching for something that doesn't exist — should return empty list (not error)
    // ─────────────────────────────────────────────────
    @Test
    void searchPosts_withNoMatch_shouldReturnEmptyList() throws Exception {

        mockMvc.perform(get("/api/v1/search/posts")
                        .param("q", "xyznonexistentkeyword999"))
                .andExpect(status().isOk())                              // HTTP 200 — not a 404
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());                     // empty array []
    }
}
