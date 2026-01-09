package com.example.social.repository;

import com.example.social.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(Long senderId1, Long receiverId1,
            Long receiverId2, Long senderId2);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    List<Message> findByReceiverIdAndSenderIdAndIsReadFalse(Long receiverId, Long senderId);
}
