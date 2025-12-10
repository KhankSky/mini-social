package com.example.social.controller;

import com.example.social.dto.request.message.ReqSendMessageDTO;
import com.example.social.dto.response.message.ResMessageDTO;
import com.example.social.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ReqSendMessageDTO req, Principal principal) {
        // For simple text messages without files, we can use this.
        // But our primary method is REST to support files.
        // This is just for fallback or lightweight text-only support.
        messageService.sendMessage(req, null, principal.getName());
    }
}
