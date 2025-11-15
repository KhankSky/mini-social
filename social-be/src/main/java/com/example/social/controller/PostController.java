package com.example.social.controller;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.dto.request.post.ReqCreatePostDTO;
import com.example.social.dto.response.filter.ResultPaginationDTO;
import com.example.social.dto.response.post.ResCreatePostDTO;
import com.example.social.service.PostService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    private final PostService postService;
    
    public PostController(PostService postService) {
        this.postService = postService;
    }
    
    // Đăng bài viết với ảnh
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResCreatePostDTO> createPost(
            @RequestPart(value = "content", required = false) String content,
            @RequestPart(value = "images", required = true) List<org.springframework.web.multipart.MultipartFile> images) throws IOException, ResourceNotFoundException {
        
        ReqCreatePostDTO reqPost = ReqCreatePostDTO.builder()
                .content(content)
                .images(images)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(reqPost));
    }
    
    // Lấy tất cả bài viết (theo thời gian)
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }
    
    // Lấy bài viết của bạn bè
    @GetMapping("/friends")
    public ResponseEntity<ResultPaginationDTO> getFriendsPosts(
            @RequestParam List<Long> friendIds,
            Pageable pageable) {
        return ResponseEntity.ok(postService.getFriendsPosts(friendIds, pageable));
    }
    
    // Lấy bài viết của một user
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResultPaginationDTO> getUserPosts(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(postService.getUserPosts(userId, pageable));
    }
}