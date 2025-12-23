package com.example.social.dto.response.user;

import com.example.social.domain.Role;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResGetUserDTO {
    private Long id;
    private String email;
    private String username;
    private String avatarUrl;
    private String bio;
    private String relationStatus;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
