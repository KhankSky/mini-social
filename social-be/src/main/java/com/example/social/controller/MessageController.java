package com.example.social.controller;

import com.example.social.dto.request.message.ReqSendMessageDTO;
import com.example.social.dto.response.message.ResConversationDTO;
import com.example.social.dto.response.message.ResMessageDTO;
import com.example.social.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ResMessageDTO> sendMessage(
            @ModelAttribute ReqSendMessageDTO req,
            @RequestParam(required = false) List<MultipartFile> files,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.sendMessage(req, files, authentication.getName()));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ResConversationDTO>> getConversations(Authentication authentication) {
        return ResponseEntity.ok(messageService.getConversations(authentication.getName()));
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ResMessageDTO>> getConversationMessages(
            @PathVariable Long userId,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.getConversationMessages(userId, authentication.getName()));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/conversations/{userId}/read")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable Long userId,
            Authentication authentication) {
        messageService.markAllAsRead(userId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(messageService.getUnreadCount(authentication.getName()));
    }
}
