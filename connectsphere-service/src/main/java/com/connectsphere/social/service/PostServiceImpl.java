package com.connectsphere.social.service;

import com.connectsphere.social.dto.CreatePostRequest;
import com.connectsphere.social.dto.PostResponse;
import com.connectsphere.social.dto.UpdatePostRequest;
import com.connectsphere.social.model.Post;
import com.connectsphere.social.model.PostVisibility;
import com.connectsphere.social.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public PostResponse createPost(Long authorId, CreatePostRequest request) {
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(request.content());
        post.setVisibility(request.visibility() == null ? PostVisibility.PUBLIC : request.visibility());
        return PostResponse.from(postRepository.save(post));
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        return PostResponse.from(findVisiblePost(postId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPublicFeed(Pageable pageable) {
        return postRepository.findByDeletedFalseAndVisibility(PostVisibility.PUBLIC, pageable)
                .map(PostResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUser(Long authorId, Pageable pageable) {
        return postRepository.findByDeletedFalseAndAuthorId(authorId, pageable)
                .map(PostResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String query, Pageable pageable) {
        return postRepository.findByDeletedFalseAndContentContainingIgnoreCase(query, pageable)
                .map(PostResponse::from);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = findVisiblePost(postId);
        ensureAuthor(post, userId);

        if (request.content() != null) {
            post.setContent(request.content());
        }
        if (request.visibility() != null) {
            post.setVisibility(request.visibility());
        }

        return PostResponse.from(postRepository.save(post));
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = findVisiblePost(postId);
        ensureAuthor(post, userId);
        post.setDeleted(true);
        postRepository.save(post);
    }

    private Post findVisiblePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (post.isDeleted()) {
            throw new IllegalArgumentException("Post not found");
        }
        return post;
    }

    private void ensureAuthor(Post post, Long userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Only the author can modify this post");
        }
    }
}

