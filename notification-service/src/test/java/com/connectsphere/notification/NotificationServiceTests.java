package com.connectsphere.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for notification-service.
 *
 * We test creating notifications and marking them as read.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NotificationServiceTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────
    // TEST 1: Create a notification, then mark it as read
    // ─────────────────────────────────────────────────
    @Test
    void createNotification_thenMarkAsRead_shouldWork() throws Exception {

        // Step 1: Create a notification (user 1 gets notified that user 2 liked their post)
        Map<String, Object> createBody = Map.of(
                "userId", 1L,
                "actorId", 2L,
                "type", "LIKE",
                "message", "Someone liked your post"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isCreated())                         // HTTP 201
                .andExpect(jsonPath("$.read").value(false))              // initially unread
                .andReturn();

        // Get the notification ID from the response
        Long notificationId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("notificationId").asLong();

        // Step 2: User 1 marks the notification as read
        mockMvc.perform(put("/api/v1/notifications/" + notificationId + "/read")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));              // now it's read!
    }

    // ─────────────────────────────────────────────────
    // TEST 2: A different user should NOT be able to mark someone else's notification as read
    // ─────────────────────────────────────────────────
    @Test
    void markRead_byWrongUser_shouldFail() throws Exception {

        // User 5 gets a notification
        Map<String, Object> createBody = Map.of(
                "userId", 5L,
                "actorId", 3L,
                "type", "FOLLOW",
                "message", "Someone followed you"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isCreated())
                .andReturn();

        Long notificationId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("notificationId").asLong();

        // User 99 tries to mark User 5's notification as read — should fail!
        mockMvc.perform(put("/api/v1/notifications/" + notificationId + "/read")
                        .param("userId", "99"))
                .andExpect(status().isBadRequest());                     // HTTP 400
    }
}
