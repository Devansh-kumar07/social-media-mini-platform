package com.connectsphere.media.service;

import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.model.MediaFile;
import com.connectsphere.media.model.MediaKind;
import com.connectsphere.media.repository.MediaRepository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaServiceImpl implements MediaService {
    private final MediaRepository mediaRepository;
    private final Path storageRoot;

    public MediaServiceImpl(MediaRepository mediaRepository, @Value("${connectsphere.media.storage-path}") String storagePath) {
        this.mediaRepository = mediaRepository;
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create media storage folder", ex);
        }
    }

    @Override
    @Transactional
    public MediaResponse upload(Long ownerId, boolean story, Long linkedPostId, String caption, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "-" + originalName.replace(" ", "_");
        Path targetPath = storageRoot.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not save file");
        }

        MediaFile mediaFile = new MediaFile();
        mediaFile.setOwnerId(ownerId);
        mediaFile.setOriginalFileName(originalName);
        mediaFile.setStoredFileName(storedName);
        mediaFile.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        mediaFile.setFileSize(file.getSize());
        mediaFile.setLinkedPostId(linkedPostId);
        mediaFile.setCaption(caption);
        mediaFile.setMediaKind(detectKind(mediaFile.getContentType()));
        mediaFile.setStory(story);
        mediaFile.setStoragePath(targetPath.toString());

        if (story) {
            mediaFile.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        }

        return MediaResponse.from(mediaRepository.save(mediaFile));
    }

    @Override
    @Transactional(readOnly = true)
    public MediaResponse getMedia(Long mediaId) {
        return MediaResponse.from(findActiveMedia(mediaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> listByOwner(Long ownerId) {
        return mediaRepository.findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(ownerId).stream()
                .map(MediaResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> getMediaByPost(Long postId) {
        return mediaRepository.findByLinkedPostIdAndDeletedFalseOrderByCreatedAtAsc(postId).stream()
                .map(MediaResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> getActiveStories() {
        return mediaRepository.findByStoryTrueAndDeletedFalseAndExpiresAtAfterOrderByCreatedAtDesc(Instant.now()).stream()
                .map(MediaResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> getStoriesByUser(Long ownerId) {
        return mediaRepository.findByStoryTrueAndOwnerIdAndDeletedFalseOrderByCreatedAtDesc(ownerId).stream()
                .filter(mediaFile -> mediaFile.getExpiresAt() == null || mediaFile.getExpiresAt().isAfter(Instant.now()))
                .map(MediaResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public MediaResponse viewStory(Long mediaId) {
        MediaFile mediaFile = findActiveMedia(mediaId);
        if (!mediaFile.isStory()) {
            throw new IllegalArgumentException("Story not found");
        }
        if (mediaFile.getExpiresAt() != null && mediaFile.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Story has expired");
        }
        mediaFile.setViewsCount(mediaFile.getViewsCount() + 1);
        return MediaResponse.from(mediaRepository.save(mediaFile));
    }

    @Override
    @Transactional
    public void deleteStory(Long mediaId, Long ownerId) {
        MediaFile mediaFile = findActiveMedia(mediaId);
        if (!mediaFile.isStory()) {
            throw new IllegalArgumentException("Story not found");
        }
        if (!mediaFile.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can delete this story");
        }
        mediaFile.setDeleted(true);
        mediaRepository.save(mediaFile);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource download(Long mediaId) {
        MediaFile mediaFile = findActiveMedia(mediaId);

        try {
            Resource resource = new UrlResource(Paths.get(mediaFile.getStoragePath()).toUri());
            if (!resource.exists()) {
                throw new IllegalArgumentException("File not found on disk");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("File path is invalid");
        }
    }

    @Override
    @Transactional
    public void delete(Long mediaId, Long ownerId) {
        MediaFile mediaFile = findActiveMedia(mediaId);
        if (!mediaFile.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can delete this media");
        }
        mediaFile.setDeleted(true);
        mediaRepository.save(mediaFile);
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 300000)
    public void expireOldStories() {
        for (MediaFile mediaFile : mediaRepository.findByStoryTrueAndDeletedFalseAndExpiresAtBefore(Instant.now())) {
            mediaFile.setDeleted(true);
            mediaRepository.save(mediaFile);
        }
    }

    private MediaFile findActiveMedia(Long mediaId) {
        MediaFile mediaFile = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        if (mediaFile.isDeleted()) {
            throw new IllegalArgumentException("Media not found");
        }
        return mediaFile;
    }

    private MediaKind detectKind(String contentType) {
        if (contentType.startsWith("image/")) {
            return MediaKind.IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return MediaKind.VIDEO;
        }
        return MediaKind.OTHER;
    }
}
