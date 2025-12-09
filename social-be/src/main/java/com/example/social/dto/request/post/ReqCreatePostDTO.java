package com.example.social.dto.request.post;

import com.example.social.domain.PostPrivacy;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReqCreatePostDTO {
    
    private String content; 
    
    private List<org.springframework.web.multipart.MultipartFile> images; 
    
    private PostPrivacy privacy;
    
    private String location; 
}