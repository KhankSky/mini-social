package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Attachment;
import com.example.social.domain.AttachmentUsage;
import com.example.social.domain.Post;
import com.example.social.domain.User;
import com.example.social.dto.request.post.ReqCreatePostDTO;
import com.example.social.dto.response.filter.Pagination;
import com.example.social.dto.response.filter.ResultPaginationDTO;
import com.example.social.dto.response.post.ResCreatePostDTO;
import com.example.social.dto.response.post.ResGetPostDTO;
import com.example.social.repository.AttachmentRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import com.example.social.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    
    public PostService(PostRepository postRepository, 
                      UserRepository userRepository,
                      AttachmentRepository attachmentRepository,
                      FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
    }
    
    @Transactional
    public ResCreatePostDTO createPost(ReqCreatePostDTO reqPost) throws IOException, ResourceNotFoundException {
        // Lấy user hiện tại từ JWT
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated"));
        
        User user = userRepository.findByEmail(currentUserLogin);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        
        // Tạo Post
        Post post = Post.builder()
                .content(reqPost.getContent())
                .user(user)
                .build();
        
        Post savedPost = postRepository.save(post);
        
        // Lưu các file ảnh
        List<ResCreatePostDTO.AttachmentDTO> attachmentDTOs = new ArrayList<>();
        if (reqPost.getImages() != null && !reqPost.getImages().isEmpty()) {
            for (MultipartFile image : reqPost.getImages()) {
                if (!image.isEmpty()) {
                    String fileUrl = fileStorageService.storeFile(image);
                    
                    Attachment attachment = Attachment.builder()
                            .fileName(image.getOriginalFilename())
                            .fileUrl(fileUrl)
                            .fileType(image.getContentType())
                            .fileSize(image.getSize())
                            .owner(user)
                            .post(savedPost)
                            .usedFor(AttachmentUsage.POST)
                            .build();
                    
                    Attachment savedAttachment = attachmentRepository.save(attachment);
                    
                    attachmentDTOs.add(ResCreatePostDTO.AttachmentDTO.builder()
                            .id(savedAttachment.getId())
                            .fileName(savedAttachment.getFileName())
                            .fileUrl(savedAttachment.getFileUrl())
                            .fileType(savedAttachment.getFileType())
                            .build());
                }
            }
        }
        
        // Build response
        return ResCreatePostDTO.builder()
                .id(savedPost.getId())
                .content(savedPost.getContent())
                .userId(user.getId())
                .username(user.getUsername())
                .userAvatarUrl(user.getAvatarUrl())
                .attachments(attachmentDTOs)
                .createdAt(savedPost.getCreatedAt())
                .updatedAt(savedPost.getUpdatedAt())
                .build();
    }
    
    // Lấy tất cả bài viết theo thời gian
    public ResultPaginationDTO getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return buildPaginationResponse(posts, pageable);
    }
    
    // Lấy bài viết của bạn bè (cần implement logic lấy danh sách bạn bè trước)
    public ResultPaginationDTO getFriendsPosts(List<Long> friendIds, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdsOrderByCreatedAtDesc(friendIds, pageable);
        return buildPaginationResponse(posts, pageable);
    }
    
    // Lấy bài viết của một user
    public ResultPaginationDTO getUserPosts(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return buildPaginationResponse(posts, pageable);
    }
    
    private ResultPaginationDTO buildPaginationResponse(Page<Post> posts, Pageable pageable) {
        Pagination pagination = new Pagination();
        pagination.setPage(pageable.getPageNumber() + 1);
        pagination.setSize(pageable.getPageSize());
        pagination.setTotalPages(posts.getTotalPages());
        pagination.setTotalElements(posts.getTotalElements());
        
        List<ResGetPostDTO> postDTOs = posts.getContent().stream()
                .map(this::toGetPostDTO)
                .collect(Collectors.toList());
        
        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setPagination(pagination);
        result.setResult(postDTOs);
        
        return result;
    }
    
    private ResGetPostDTO toGetPostDTO(Post post) {
        // Load attachments
        List<Attachment> attachments = attachmentRepository.findByPostId(post.getId());
        
        List<ResGetPostDTO.AttachmentDTO> attachmentDTOs = attachments.stream()
                .map(att -> ResGetPostDTO.AttachmentDTO.builder()
                        .id(att.getId())
                        .fileName(att.getFileName())
                        .fileUrl(att.getFileUrl())
                        .fileType(att.getFileType())
                        .build())
                .collect(Collectors.toList());
        
        return ResGetPostDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .userAvatarUrl(post.getUser().getAvatarUrl())
                .attachments(attachmentDTOs)
                .likeCount((long) post.getLikes().size())
                .commentCount((long) post.getComments().size())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}