package com.example.social.controller;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.dto.request.comment.ReqCreateCommentDTO;
import com.example.social.dto.response.comment.ResCommentDTO;
import com.example.social.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ResCommentDTO> createComment(@PathVariable Long postId,
                                                       @Valid @RequestBody ReqCreateCommentDTO request) throws ResourceNotFoundException {
        ResCommentDTO response = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResCommentDTO>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }
}


