package com.connectsphere.auth.client;

import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MediaServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.media-url:http://localhost:8083}")
    private String mediaServiceUrl;

    public MediaServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String uploadProfilePicture(Long ownerId, MultipartFile file) {
        try {
            String url = mediaServiceUrl + "/api/v1/media/upload";

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(
                    Objects.requireNonNullElse(file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ));

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(fileResource, fileHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("ownerId", ownerId.toString());
            body.add("story", "false");
            body.add("file", fileEntity);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ResponseEntity<MediaUploadResponse> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    MediaUploadResponse.class
            );

            MediaUploadResponse payload = response.getBody();
            if (payload == null || payload.downloadUrl() == null || payload.downloadUrl().isBlank()) {
                throw new IllegalArgumentException("Media service did not return a profile picture URL");
            }
            return payload.downloadUrl();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not read profile picture file", ex);
        } catch (Exception ex) {
            log.error("Could not upload profile picture for user {}. Reason: {}", ownerId, ex.getMessage());
            throw new IllegalArgumentException("Could not upload profile picture");
        }
    }

    private record MediaUploadResponse(String downloadUrl) {
    }
}
