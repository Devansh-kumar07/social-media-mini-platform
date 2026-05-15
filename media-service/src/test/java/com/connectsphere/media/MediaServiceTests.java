package com.connectsphere.media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for media-service (file uploads).
 *
 * MockMultipartFile lets us fake a file upload without a real file on disk.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MediaServiceTests {

    @Autowired
    MockMvc mockMvc;

    // ─────────────────────────────────────────────────
    // TEST 1: Upload a valid image — should return metadata
    // ─────────────────────────────────────────────────
    @Test
    void uploadValidImage_shouldReturnMediaResponse() throws Exception {

        // Create a fake image file (MockMultipartFile = fake file for testing)
        MockMultipartFile fakeImage = new MockMultipartFile(
                "file",                    // form field name (must match controller)
                "photo.jpg",               // original filename
                "image/jpeg",              // content type
                "fake-image-bytes".getBytes()  // some fake bytes
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(fakeImage)
                        .param("ownerId", "1")
                        .param("story", "false"))
                .andExpect(status().isCreated())                           // HTTP 201
                .andExpect(jsonPath("$.originalFileName").value("photo.jpg"))
                .andExpect(jsonPath("$.mediaKind").value("IMAGE"))         // detected as IMAGE
                .andExpect(jsonPath("$.story").value(false));
    }

    // ─────────────────────────────────────────────────
    // TEST 2: Upload an empty file — should return 400 Bad Request
    // ─────────────────────────────────────────────────
    @Test
    void uploadEmptyFile_shouldReturn400() throws Exception {

        // Empty file — 0 bytes
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]   // no content at all
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(emptyFile)
                        .param("ownerId", "1")
                        .param("story", "false"))
                .andExpect(status().isBadRequest());                       // HTTP 400
    }

    @Test
    void uploadStory_shouldSetExpiry() throws Exception {

        MockMultipartFile fakeStory = new MockMultipartFile(
                "file",
                "story.mp4",
                "video/mp4",
                "fake-video-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(fakeStory)
                        .param("ownerId", "9")
                        .param("story", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.story").value(true))
                .andExpect(jsonPath("$.mediaKind").value("VIDEO"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }
}
