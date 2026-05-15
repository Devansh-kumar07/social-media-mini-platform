package com.connectsphere.social.client;

import com.connectsphere.social.dto.UploadedMediaResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
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

    public UploadedMediaResponse uploadPostMedia(Long ownerId, Long postId, MultipartFile file) {
        try {
            String url = mediaServiceUrl + "/api/v1/media/upload";

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(
                    file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType()
            ));
            fileHeaders.setContentDisposition(ContentDisposition.builder("form-data")
                    .name("file")
                    .filename(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                    .build());

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("ownerId", ownerId.toString());
            body.add("story", "false");
            body.add("linkedPostId", postId.toString());
            body.add("file", new HttpEntity<>(fileResource, fileHeaders));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ResponseEntity<UploadedMediaResponse> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    UploadedMediaResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalArgumentException("Media upload failed");
            }

            return response.getBody();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not read uploaded file", ex);
        } catch (Exception ex) {
            log.error("Could not upload media for post {}. Reason: {}", postId, ex.getMessage());
            throw new IllegalArgumentException("Could not upload media");
        }
    }
}
