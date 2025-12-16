package com.example.social.service;

import com.example.social.domain.Attachment;
import com.example.social.domain.AttachmentUsage;
import com.example.social.domain.Message;
import com.example.social.domain.User;
import com.example.social.dto.request.message.ReqSendMessageDTO;
import com.example.social.dto.response.message.ResConversationDTO;
import com.example.social.dto.response.message.ResMessageDTO;
import com.example.social.repository.MessageRepository;
import com.example.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ResMessageDTO sendMessage(ReqSendMessageDTO req, List<MultipartFile> files, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail);
        if (sender == null)
            throw new RuntimeException("Sender not found");

        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(req.getContent());
        message.setIsRead(false);

        // Handle attachments
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String fileUrl = fileStorageService.storeFile(file);
                    Attachment attachment = new Attachment();
                    attachment.setFileUrl(fileUrl);
                    attachment.setFileType(file.getContentType());
                    attachment.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
                    attachment.setFileSize(file.getSize());
                    attachment.setOwner(sender);
                    attachment.setMessage(message);
                    attachment.setUsedFor(AttachmentUsage.MESSAGE);
                    message.getAttachments().add(attachment);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store file", e);
                }
            }
        }

        Message savedMessage = messageRepository.save(message);
        ResMessageDTO resDto = convertToDTO(savedMessage);

        // Notify receiver via WebSocket
        // IMPORTANT: Use email instead of username because WebSocket authentication
        // uses email as principal
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/messages", resDto);

        return resDto;
    }

    @Transactional
    public ResMessageDTO editMessage(Long messageId, String newContent, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail);
        if (sender == null)
            throw new RuntimeException("User not found");

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(sender.getId())) {
            throw new RuntimeException("You are not authorized to edit this message");
        }

        message.setContent(newContent);
        Message savedMessage = messageRepository.save(message);
        ResMessageDTO resDto = convertToDTO(savedMessage);

        // Notify receiver via WebSocket
        messagingTemplate.convertAndSendToUser(message.getReceiver().getEmail(), "/queue/messages", resDto);

        // Also notify sender to update their UI immediately (optional, but good for
        // consistency across devices)
        // actually sender gets the response from API, but if they have multiple tabs
        // open...
        // let's just stick to notifying receiver. Request/Response handles sender.

        return resDto;
    }

    public List<ResMessageDTO> getConversationMessages(Long userId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null)
            throw new RuntimeException("User not found");

        List<Message> messages = messageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(
                currentUser.getId(), userId,
                userId, currentUser.getId());

        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ResConversationDTO> getConversations(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null)
            throw new RuntimeException("User not found");

        // Optimization: In real app, use @Query. For now, we filter in memory but this
        // is heavy.
        List<Message> allMessages = messageRepository.findAll();

        Map<Long, Message> lastMessageMap = new HashMap<>();
        Map<Long, Integer> unreadCountMap = new HashMap<>();

        for (Message m : allMessages) {
            boolean isSender = m.getSender().getId().equals(currentUser.getId());
            boolean isReceiver = m.getReceiver().getId().equals(currentUser.getId());

            if (!isSender && !isReceiver)
                continue;

            Long otherUserId = isSender ? m.getReceiver().getId() : m.getSender().getId();

            // Update last message
            if (!lastMessageMap.containsKey(otherUserId)
                    || m.getSentAt().isAfter(lastMessageMap.get(otherUserId).getSentAt())) {
                lastMessageMap.put(otherUserId, m);
            }

            // Count unread
            if (isReceiver && !(m.getIsRead() != null && m.getIsRead())) {
                unreadCountMap.put(otherUserId, unreadCountMap.getOrDefault(otherUserId, 0) + 1);
            }
        }

        List<ResConversationDTO> conversations = new ArrayList<>();
        for (Long otherUserId : lastMessageMap.keySet()) {
            Message lastMsg = lastMessageMap.get(otherUserId);
            User otherUser = lastMsg.getSender().getId().equals(currentUser.getId()) ? lastMsg.getReceiver()
                    : lastMsg.getSender();

            ResConversationDTO dto = new ResConversationDTO();
            dto.setUserId(otherUser.getId());
            dto.setUsername(otherUser.getUsername());
            dto.setEmail(otherUser.getEmail());
            dto.setAvatarUrl(otherUser.getAvatarUrl());
            dto.setLastMessage(lastMsg.getContent());
            dto.setLastMessageTime(lastMsg.getSentAt());
            dto.setUnreadCount(unreadCountMap.getOrDefault(otherUserId, 0));

            conversations.add(dto);
        }

        conversations.sort(Comparator.comparing(ResConversationDTO::getLastMessageTime).reversed());
        return conversations;
    }

    @Transactional
    public void markAsRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            m.setIsRead(true);
            messageRepository.save(m);
        });
    }

    @Transactional
    public void markAllAsRead(Long senderId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null)
            return;
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndSenderIdAndIsReadFalse(currentUser.getId(),
                senderId);
        unreadMessages.forEach(m -> m.setIsRead(true));
        messageRepository.saveAll(unreadMessages);
    }

    public long getUnreadCount(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null)
            return 0;
        return messageRepository.countByReceiverIdAndIsReadFalse(currentUser.getId());
    }

    private ResMessageDTO convertToDTO(Message message) {
        ResMessageDTO dto = new ResMessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getUsername());
        dto.setSenderAvatar(message.getSender().getAvatarUrl());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setContent(message.getContent());
        dto.setRead(message.getIsRead() != null && message.getIsRead());
        dto.setSentAt(message.getSentAt());

        List<String> attachmentUrls = message.getAttachments().stream()
                .map(Attachment::getFileUrl)
                .collect(Collectors.toList());
        dto.setAttachments(attachmentUrls);

        return dto;
    }
}
