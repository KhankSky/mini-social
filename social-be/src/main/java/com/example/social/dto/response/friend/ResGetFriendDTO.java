package com.example.social.dto.response.friend;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResGetFriendDTO {

    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime friendsSince;
}
