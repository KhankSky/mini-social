package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Post;
import com.example.social.domain.PostLike;
import com.example.social.domain.User;
import com.example.social.repository.PostLikeRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void likePost(Long postId, String userEmail) throws ResourceNotFoundException {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Check if already liked
        if (postLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            return; // Already liked, do nothing
        }

        // Create like
        PostLike like = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        postLikeRepository.save(like);

        // Send notification to post owner
        if (!post.getUser().getId().equals(user.getId())) {
            notificationService.createNotification(
                    post.getUser(),
                    user,
                    "LIKE",
                    user.getUsername() + " liked your post",
                    postId.toString());
        }
    }

    @Transactional
    public void unlikePost(Long postId, String userEmail) throws ResourceNotFoundException {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Find and delete like
        postLikeRepository.findByPostIdAndUserId(postId, user.getId())
                .ifPresent(postLikeRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isPostLikedByUser(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            return false;
        }
        return postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
    }

    @Transactional(readOnly = true)
    public long getLikeCountForPost(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}