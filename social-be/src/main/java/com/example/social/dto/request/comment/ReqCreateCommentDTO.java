package com.example.social.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqCreateCommentDTO {

    @NotBlank(message = "Content is required")
    private String content;

    private Long parentCommentId;
}



