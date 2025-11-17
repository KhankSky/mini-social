package com.example.social.dto.response.comment;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResCommentDTO {

    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String userAvatarUrl;
    private String content;
    private Long parentCommentId;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<ResCommentDTO> replies = new ArrayList<>();
}



