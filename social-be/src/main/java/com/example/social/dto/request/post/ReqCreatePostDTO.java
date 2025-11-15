package com.example.social.dto.request.post;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReqCreatePostDTO {
    
    private String content; // Nội dung bài viết (có thể null nếu chỉ có ảnh)
    
    @NotEmpty(message = "At least one image is required")
    private List<MultipartFile> images; // Danh sách ảnh
}