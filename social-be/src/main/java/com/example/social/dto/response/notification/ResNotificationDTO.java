package com.example.social.dto.response.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResNotificationDTO {
    private Long id;
    private Long actorId;
    private String actorName;
    private String actorAvatar;
    private String type;
    private String content;
    private String referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
