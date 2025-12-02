package com.example.social.dto.response.post;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResGetPostDTO {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private String userAvatarUrl;
    private List<AttachmentDTO> attachments;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class AttachmentDTO {
        private Long id;
        private String fileName;
        private String fileUrl;
        private String fileType;
    }
}