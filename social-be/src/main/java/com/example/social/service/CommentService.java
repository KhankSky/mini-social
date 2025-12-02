package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Comment;
import com.example.social.domain.Post;
import com.example.social.domain.User;
import com.example.social.dto.request.comment.ReqCreateCommentDTO;
import com.example.social.dto.response.comment.ResCommentDTO;
import com.example.social.repository.CommentRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import com.example.social.security.SecurityUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public ResCommentDTO createComment(Long postId, ReqCreateCommentDTO request) throws ResourceNotFoundException {
        User currentUser = resolveCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));

            if (!parentComment.getPost().getId().equals(postId)) {
                throw new ResourceNotFoundException("Parent comment does not belong to the same post");
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(currentUser)
                .content(request.getContent())
                .parentComment(parentComment)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Notify the post owner
        notifyPostOwner(post, savedComment, currentUser);

        return mapToDTO(savedComment);
    }

    @Transactional(readOnly = true)
    public List<ResCommentDTO> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);
        return comments.stream()
                .map(this::mapToDTOWithChildren)
                .collect(Collectors.toList());
    }

    private ResCommentDTO mapToDTO(Comment comment) {
        return ResCommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userAvatarUrl(comment.getUser().getAvatarUrl())
                .content(comment.getContent())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private ResCommentDTO mapToDTOWithChildren(Comment comment) {
        ResCommentDTO dto = mapToDTO(comment);
        List<ResCommentDTO> children = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(comment.getId()).stream()
                .map(this::mapToDTOWithChildren)
                .collect(Collectors.toList());
        dto.setReplies(children);
        return dto;
    }

    private User resolveCurrentUser() throws ResourceNotFoundException {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated"));
        User user = userRepository.findByEmail(currentUserLogin);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return user;
    }

    private void notifyPostOwner(Post post, Comment comment, User commenter) {
        User postOwner = post.getUser();
        if (postOwner == null || postOwner.getId().equals(commenter.getId())) {
            return;
        }

        NotificationPayload payload = new NotificationPayload(
                "COMMENT",
                post.getId(),
                comment.getId(),
                commenter.getUsername(),
                comment.getContent()
        );
        messagingTemplate.convertAndSendToUser(
                postOwner.getEmail(),
                "/queue/notifications",
                payload
        );
    }

    public record NotificationPayload(
            String type,
            Long postId,
            Long commentId,
            String fromUser,
            String commentContent
    ) {
    }
}

