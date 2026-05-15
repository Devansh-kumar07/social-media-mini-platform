package com.connectsphere.social.service;

import com.connectsphere.social.client.NotificationServiceClient;
import com.connectsphere.social.dto.CreateReactionRequest;
import com.connectsphere.social.dto.ReactionResponse;
import com.connectsphere.social.dto.ReactionSummaryResponse;
import com.connectsphere.social.dto.UpdateReactionRequest;
import com.connectsphere.social.model.Comment;
import com.connectsphere.social.model.Post;
import com.connectsphere.social.model.Reaction;
import com.connectsphere.social.model.ReactionTargetType;
import com.connectsphere.social.repository.CommentRepository;
import com.connectsphere.social.repository.PostRepository;
import com.connectsphere.social.repository.ReactionRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // ✅ NEW: Notification client to tell users when someone reacts to their post/comment
    private final NotificationServiceClient notificationServiceClient;

    public ReactionServiceImpl(
            ReactionRepository reactionRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            NotificationServiceClient notificationServiceClient
    ) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    @Transactional
    public ReactionResponse react(Long userId, CreateReactionRequest request) {
        ensureTargetExists(request.targetId(), request.targetType());
        if (reactionRepository.findByUserIdAndTargetIdAndTargetType(userId, request.targetId(), request.targetType()).isPresent()) {
            throw new IllegalArgumentException("Reaction already exists for this target");
        }

        Reaction reaction = new Reaction();
        reaction.setUserId(userId);
        reaction.setTargetId(request.targetId());
        reaction.setTargetType(request.targetType());
        reaction.setReactionType(request.reactionType());

        Reaction savedReaction = reactionRepository.save(reaction);
        incrementCounter(savedReaction.getTargetId(), savedReaction.getTargetType());

        // ✅ INTEGRATION: Notify the owner of the post/comment that someone reacted
        sendReactionNotification(userId, request.targetId(), request.targetType());

        return ReactionResponse.from(savedReaction);
    }

    @Override
    @Transactional
    public ReactionResponse changeReaction(Long userId, Long targetId, ReactionTargetType targetType, UpdateReactionRequest request) {
        Reaction reaction = reactionRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)
                .orElseThrow(() -> new IllegalArgumentException("Reaction not found"));
        reaction.setReactionType(request.reactionType());
        return ReactionResponse.from(reactionRepository.save(reaction));
    }

    @Override
    @Transactional
    public void removeReaction(Long userId, Long targetId, ReactionTargetType targetType) {
        Reaction reaction = reactionRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)
                .orElseThrow(() -> new IllegalArgumentException("Reaction not found"));
        reactionRepository.delete(reaction);
        decrementCounter(targetId, targetType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReactionResponse> getReactionsByUser(Long userId) {
        return reactionRepository.findByUserId(userId).stream()
                .map(ReactionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReactionSummaryResponse getSummary(Long targetId, ReactionTargetType targetType) {
        ensureTargetExists(targetId, targetType);
        Map<String, Long> counts = new LinkedHashMap<>();
        long total = 0;

        for (Reaction reaction : reactionRepository.findByTargetIdAndTargetType(targetId, targetType)) {
            String key = reaction.getReactionType().name();
            counts.put(key, counts.getOrDefault(key, 0L) + 1);
            total++;
        }

        return new ReactionSummaryResponse(targetId, targetType, total, counts);
    }

    private void ensureTargetExists(Long targetId, ReactionTargetType targetType) {
        if (targetType == ReactionTargetType.POST) {
            Post post = postRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));
            if (post.isDeleted()) {
                throw new IllegalArgumentException("Post not found");
            }
            return;
        }

        Comment comment = commentRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Comment not found");
        }
    }

    private void incrementCounter(Long targetId, ReactionTargetType targetType) {
        if (targetType == ReactionTargetType.POST) {
            Post post = postRepository.findById(targetId).orElseThrow();
            post.setLikesCount(post.getLikesCount() + 1);
            postRepository.save(post);
            return;
        }

        Comment comment = commentRepository.findById(targetId).orElseThrow();
        comment.setLikesCount(comment.getLikesCount() + 1);
        commentRepository.save(comment);
    }

    private void decrementCounter(Long targetId, ReactionTargetType targetType) {
        if (targetType == ReactionTargetType.POST) {
            Post post = postRepository.findById(targetId).orElseThrow();
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            postRepository.save(post);
            return;
        }

        Comment comment = commentRepository.findById(targetId).orElseThrow();
        comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
        commentRepository.save(comment);
    }

    /**
     * ✅ INTEGRATION HELPER: Find who owns the post/comment and send them a notification.
     */
    private void sendReactionNotification(Long reactorUserId, Long targetId, ReactionTargetType targetType) {
        try {
            if (targetType == ReactionTargetType.POST) {
                // Find the post author and notify them
                Post post = postRepository.findById(targetId).orElse(null);
                if (post != null) {
                    notificationServiceClient.sendNotification(
                            post.getAuthorId(),           // post author gets the notification
                            reactorUserId,                // the person who reacted
                            "LIKE",
                            "Someone liked your post"
                    );
                }
            } else {
                // Find the comment author and notify them
                Comment comment = commentRepository.findById(targetId).orElse(null);
                if (comment != null) {
                    notificationServiceClient.sendNotification(
                            comment.getAuthorId(),        // comment author gets the notification
                            reactorUserId,
                            "LIKE",
                            "Someone liked your comment"
                    );
                }
            }
        } catch (Exception e) {
            // Don't crash if notification fails
        }
    }
}
