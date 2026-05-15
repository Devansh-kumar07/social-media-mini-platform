package com.connectsphere.media.controller;

import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/health")
    @Operation(summary = "Check media service health")
    public String health() {
        return "media-service is running";
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload media or create a story")
    public MediaResponse uploadMedia(
            @RequestParam(name = "ownerId") @Min(1) Long ownerId,
            @RequestParam(name = "story", defaultValue = "false") boolean story,
            @RequestParam(name = "linkedPostId", required = false) Long linkedPostId,
            @RequestParam(name = "caption", required = false) String caption,
            @RequestParam(name = "file") MultipartFile file
    ) {
        return mediaService.upload(ownerId, story, linkedPostId, caption, file);
    }

    @GetMapping("/{mediaId}")
    @Operation(summary = "Get media metadata by id")
    public MediaResponse getMedia(@PathVariable("mediaId") Long mediaId) {
        return mediaService.getMedia(mediaId);
    }

    @GetMapping("/user/{ownerId}")
    @Operation(summary = "List media by owner")
    public List<MediaResponse> listMediaByOwner(@PathVariable("ownerId") Long ownerId) {
        return mediaService.listByOwner(ownerId);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "List media linked to a post")
    public List<MediaResponse> listMediaByPost(@PathVariable("postId") Long postId) {
        return mediaService.getMediaByPost(postId);
    }

    @GetMapping("/stories/active")
    @Operation(summary = "List active stories")
    public List<MediaResponse> activeStories() {
        return mediaService.getActiveStories();
    }

    @GetMapping("/stories/user/{ownerId}")
    @Operation(summary = "List stories by user")
    public List<MediaResponse> storiesByUser(@PathVariable("ownerId") Long ownerId) {
        return mediaService.getStoriesByUser(ownerId);
    }

    @PostMapping("/stories/{mediaId}/view")
    @Operation(summary = "Increment story view count")
    public MediaResponse viewStory(@PathVariable("mediaId") Long mediaId) {
        return mediaService.viewStory(mediaId);
    }

    @DeleteMapping("/stories/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete own story")
    public void deleteStory(
            @PathVariable("mediaId") Long mediaId,
            @RequestParam(name = "ownerId") Long ownerId
    ) {
        mediaService.deleteStory(mediaId, ownerId);
    }

    @GetMapping("/download/{mediaId}")
    @Operation(summary = "Download uploaded file")
    public ResponseEntity<Resource> download(@PathVariable("mediaId") Long mediaId) {
        Resource resource = mediaService.download(mediaId);
        MediaResponse media = mediaService.getMedia(mediaId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(media.originalFileName()).build());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(media.contentType()))
                .body(resource);
    }

    @DeleteMapping("/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete media")
    public void deleteMedia(
            @PathVariable("mediaId") Long mediaId,
            @RequestParam(name = "ownerId") Long ownerId
    ) {
        mediaService.delete(mediaId, ownerId);
    }
}
