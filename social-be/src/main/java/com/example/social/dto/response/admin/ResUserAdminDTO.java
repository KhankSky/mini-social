package com.example.social.dto.response.admin;

import com.example.social.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResUserAdminDTO {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
