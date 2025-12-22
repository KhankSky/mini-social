package com.example.social.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The recipient of the notification

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor; // The user who triggered the notification

    @Column(nullable = false)
    private String type; // COMMENT, FRIEND_REQUEST, LIKE, etc.

    private String content; // Short description or preview

    private String referenceId; // ID of the related entity (postId, friendRequestId, etc.)

    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
