package com.example.social.controller;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<Void> likePost(@PathVariable Long postId) throws ResourceNotFoundException, IOException {
        String currentUserLogin = com.example.social.security.SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated"));
        postLikeService.likePost(postId, currentUserLogin);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId) throws ResourceNotFoundException {
        String currentUserLogin = com.example.social.security.SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated"));
        postLikeService.unlikePost(postId, currentUserLogin);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/post/{postId}/is-liked")
    public ResponseEntity<Boolean> isPostLikedByCurrentUser(@PathVariable Long postId) throws ResourceNotFoundException {
        String currentUserLogin = com.example.social.security.SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated"));
        boolean isLiked = postLikeService.isPostLikedByUser(postId, currentUserLogin);
        return ResponseEntity.ok(isLiked);
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getLikeCountForPost(@PathVariable Long postId) {
        long likeCount = postLikeService.getLikeCountForPost(postId);
        return ResponseEntity.ok(likeCount);
    }
}