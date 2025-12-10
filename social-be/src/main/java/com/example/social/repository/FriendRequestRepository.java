package com.example.social.repository;

import com.example.social.domain.FriendRequest;
import com.example.social.domain.FriendRequestStatus;
import com.example.social.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    // Find all requests received by a user with specific status
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);

    // Find all requests sent by a user with specific status
    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequestStatus status);

    // Find all pending requests received by a user
    List<FriendRequest> findByReceiverAndStatusOrderByCreatedAtDesc(User receiver, FriendRequestStatus status);

    // Find all pending requests sent by a user
    List<FriendRequest> findBySenderAndStatusOrderByCreatedAtDesc(User sender, FriendRequestStatus status);

    // Check if a request exists between two users (either direction)
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "(fr.sender = :user1 AND fr.receiver = :user2) OR " +
           "(fr.sender = :user2 AND fr.receiver = :user1)")
    Optional<FriendRequest> findRequestBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    // Check if a pending request exists from sender to receiver
    Optional<FriendRequest> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);

    // Find accepted requests for a user (either as sender or receiver)
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "(fr.sender = :user OR fr.receiver = :user) AND fr.status = :status")
    List<FriendRequest> findAcceptedRequestsForUser(@Param("user") User user, @Param("status") FriendRequestStatus status);
}
