package com.example.social.dto.response.friend;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResGetFriendRequestDTO {

    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderEmail;
    private String senderAvatarUrl;
    private Long receiverId;
    private String receiverUsername;
    private String receiverEmail;
    private String receiverAvatarUrl;
    private String status;
    private LocalDateTime createdAt;
}
