package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Role;
import com.example.social.domain.User;
import com.example.social.domain.Post;
import com.example.social.domain.Comment;
import com.example.social.dto.response.admin.ResAdminStatsDTO;
import com.example.social.dto.response.admin.ResUserAdminDTO;
import com.example.social.repository.UserRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Page<ResUserAdminDTO> getAllUsers(Pageable pageable, String search) {
        Page<User> users;

        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::mapToUserAdminDTO);
    }

    @Transactional
    public void updateUserRole(Long userId, Role role) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional
    public void deletePost(Long postId) throws ResourceNotFoundException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        postRepository.delete(post);
    }

    @Transactional
    public void deleteComment(Long commentId) throws ResourceNotFoundException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public ResAdminStatsDTO getStatistics() {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        // Active today (users who logged in today)
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long activeToday = userRepository.countByLastLoginAfter(startOfDay);

        // New users this week
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long newUsersThisWeek = userRepository.countByCreatedAtAfter(weekAgo);

        return ResAdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalPosts(totalPosts)
                .totalComments(totalComments)
                .activeToday(activeToday)
                .newUsersThisWeek(newUsersThisWeek)
                .build();
    }

    private ResUserAdminDTO mapToUserAdminDTO(User user) {
        return ResUserAdminDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
