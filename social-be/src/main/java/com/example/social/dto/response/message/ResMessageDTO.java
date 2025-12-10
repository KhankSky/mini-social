package com.example.social.dto.response.message;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class ResMessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private String content;
    private boolean isRead;
    private java.time.LocalDateTime sentAt;
    private List<String> attachments;
}
