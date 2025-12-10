package com.example.social.repository;

import com.example.social.domain.Friends;
import com.example.social.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {

    // Find all friends for a user (both directions)
    @Query("SELECT f FROM Friends f WHERE f.user = :user OR f.friend = :user")
    List<Friends> findAllFriendsForUser(@Param("user") User user);

    // Find specific friendship between two users (either direction)
    @Query("SELECT f FROM Friends f WHERE " +
           "(f.user = :user1 AND f.friend = :user2) OR " +
           "(f.user = :user2 AND f.friend = :user1)")
    List<Friends> findFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    // Check if two users are friends
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friends f WHERE " +
           "(f.user = :user1 AND f.friend = :user2) OR " +
           "(f.user = :user2 AND f.friend = :user1)")
    boolean areFriends(@Param("user1") User user1, @Param("user2") User user2);

    // Delete friendship between two users (both directions)
    @Query("DELETE FROM Friends f WHERE " +
           "(f.user = :user1 AND f.friend = :user2) OR " +
           "(f.user = :user2 AND f.friend = :user1)")
    void deleteFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
}
