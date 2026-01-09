package com.example.social.dto.request.message;

import lombok.Data;

@Data
public class ReqSendMessageDTO {
    private Long receiverId;
    private String content;
}
