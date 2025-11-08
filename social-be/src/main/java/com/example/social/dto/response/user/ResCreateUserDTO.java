package com.example.social.dto.response.user;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResCreateUserDTO {
    private Long id;
    private String email;
    private String username;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
}
