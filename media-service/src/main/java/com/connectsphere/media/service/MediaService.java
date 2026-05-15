package com.connectsphere.media.service;

import com.connectsphere.media.dto.MediaResponse;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    MediaResponse upload(Long ownerId, boolean story, Long linkedPostId, String caption, MultipartFile file);

    MediaResponse getMedia(Long mediaId);

    List<MediaResponse> listByOwner(Long ownerId);

    List<MediaResponse> getMediaByPost(Long postId);

    List<MediaResponse> getActiveStories();

    List<MediaResponse> getStoriesByUser(Long ownerId);

    MediaResponse viewStory(Long mediaId);

    void deleteStory(Long mediaId, Long ownerId);

    Resource download(Long mediaId);

    void delete(Long mediaId, Long ownerId);

    void expireOldStories();
}
