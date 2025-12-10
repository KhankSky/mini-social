package com.example.social.dto.response.message;

import lombok.Data;

@Data
public class ResConversationDTO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String lastMessage;
    private java.time.LocalDateTime lastMessageTime;
    private long unreadCount;
}
