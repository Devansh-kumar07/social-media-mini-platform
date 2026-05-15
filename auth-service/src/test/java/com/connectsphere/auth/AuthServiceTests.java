package com.connectsphere.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.connectsphere.auth.client.SearchServiceClient;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for auth-service.
 *
 * @SpringBootTest — starts the full Spring application
 * @AutoConfigureMockMvc — lets us send fake HTTP requests without a real server
 * @MockBean SearchServiceClient — we don't want real HTTP calls to search-service in tests
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthServiceTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // This replaces the real SearchServiceClient with a "fake" that does nothing
    // So our tests don't fail trying to reach a real search-service
    @MockBean
    SearchServiceClient searchServiceClient;

    // ─────────────────────────────────────────────────
    // TEST 1: Register a new user and then log in
    // ─────────────────────────────────────────────────
    @Test
    void registerAndLogin_shouldReturnJwtToken() throws Exception {

        // Step 1: Register a new user
        Map<String, String> registerBody = Map.of(
                "username", "testuser1",
                "email", "testuser1@example.com",
                "password", "password123",
                "fullName", "Test User One"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated())                         // HTTP 201
                .andExpect(jsonPath("$.email").value("testuser1@example.com"));

        // Step 2: Login with the same credentials
        Map<String, String> loginBody = Map.of(
                "email", "testuser1@example.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())                              // HTTP 200
                .andExpect(jsonPath("$.accessToken").isNotEmpty())       // token must exist
                .andExpect(jsonPath("$.tokenType").value("Bearer"));     // must be Bearer type
    }

    // ─────────────────────────────────────────────────
    // TEST 2: Registering with an already-used email should fail
    // ─────────────────────────────────────────────────
    @Test
    void register_withDuplicateEmail_shouldReturn400() throws Exception {

        // First registration — should succeed
        Map<String, String> firstUser = Map.of(
                "username", "uniqueuser1",
                "email", "duplicate@example.com",
                "password", "password123",
                "fullName", "First User"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // Second registration with SAME email — should fail with 400 Bad Request
        Map<String, String> secondUser = Map.of(
                "username", "uniqueuser2",
                "email", "duplicate@example.com",   // same email!
                "password", "password123",
                "fullName", "Second User"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isBadRequest());                     // HTTP 400
    }
}
