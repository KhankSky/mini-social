package com.example.social.dto.response.user;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResUpdateUserDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private String bio;
    private LocalDateTime updatedAt;
}
