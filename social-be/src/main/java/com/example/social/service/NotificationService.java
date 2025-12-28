package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Notification;
import com.example.social.domain.User;
import com.example.social.dto.response.notification.ResNotificationDTO;
import com.example.social.repository.NotificationRepository;
import com.example.social.repository.UserRepository;
import com.example.social.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(User recipient, User actor, String type, String content, String referenceId) {
        if (recipient.getId().equals(actor.getId())) {
            return; // Don't notify self
        }

        Notification notification = Notification.builder()
                .user(recipient)
                .actor(actor)
                .type(type)
                .content(content)
                .referenceId(referenceId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Send real-time update
        ResNotificationDTO dto = mapToDTO(saved);
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                dto);
    }

    @Transactional(readOnly = true)
    public List<ResNotificationDTO> getMyNotifications() throws ResourceNotFoundException {
        User currentUser = resolveCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) throws ResourceNotFoundException {
        User currentUser = resolveCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Not authorized");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() throws ResourceNotFoundException {
        User currentUser = resolveCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    private ResNotificationDTO mapToDTO(Notification notification) {
        return ResNotificationDTO.builder()
                .id(notification.getId())
                .actorId(notification.getActor().getId())
                .actorName(notification.getActor().getUsername())
                .actorAvatar(notification.getActor().getAvatarUrl())
                .type(notification.getType())
                .content(notification.getContent())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private User resolveCurrentUser() throws ResourceNotFoundException {
        String email = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated"));
        return userRepository.findByEmail(email);
    }
}
