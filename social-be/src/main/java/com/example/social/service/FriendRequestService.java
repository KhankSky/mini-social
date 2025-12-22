package com.example.social.service;

import com.example.social.controller.error.ResourceAlreadyExistsException;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.FriendRequest;
import com.example.social.domain.FriendRequestStatus;
import com.example.social.domain.Friends;
import com.example.social.domain.User;
import com.example.social.dto.request.friend.ReqSendFriendRequestDTO;
import com.example.social.dto.response.friend.ResGetFriendDTO;
import com.example.social.dto.response.friend.ResGetFriendRequestDTO;
import com.example.social.repository.FriendRequestRepository;
import com.example.social.repository.FriendsRepository;
import com.example.social.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendsRepository friendsRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FriendRequestService(FriendRequestRepository friendRequestRepository,
            FriendsRepository friendsRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendsRepository = friendsRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    @Transactional
    public ResGetFriendRequestDTO sendFriendRequest(ReqSendFriendRequestDTO request)
            throws ResourceNotFoundException, ResourceAlreadyExistsException {

        User sender = getCurrentUser();
        if (sender == null) {
            throw new ResourceNotFoundException("Current user not found");
        }

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id = " + request.getReceiverId()));

        // Check if trying to send request to self
        if (sender.getId().equals(receiver.getId())) {
            throw new ResourceAlreadyExistsException("Cannot send friend request to yourself");
        }

        // Check if already friends
        if (friendsRepository.areFriends(sender, receiver)) {
            throw new ResourceAlreadyExistsException("You are already friends with this user");
        }

        // Check if a request already exists between these users (either direction)
        Optional<FriendRequest> existingRequest = friendRequestRepository.findRequestBetweenUsers(sender, receiver);
        if (existingRequest.isPresent()) {
            FriendRequest existing = existingRequest.get();
            if (existing.getStatus() == FriendRequestStatus.PENDING) {
                throw new ResourceAlreadyExistsException("A friend request already exists between you and this user");
            }
            // If there's a rejected request, we can allow a new one
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        FriendRequest saved = friendRequestRepository.save(friendRequest);
        log.info("Friend request sent from user {} to user {}", sender.getId(), receiver.getId());

        // Notify receiver
        notificationService.createNotification(
                receiver,
                sender,
                "FRIEND_REQUEST",
                sender.getUsername() + " sent you a friend request",
                String.valueOf(saved.getId()));

        return toFriendRequestDTO(saved);
    }

    @Transactional
    public ResGetFriendRequestDTO acceptFriendRequest(Long requestId)
            throws ResourceNotFoundException {

        User currentUser = getCurrentUser();
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with id = " + requestId));

        // Verify that the current user is the receiver
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You can only accept requests sent to you");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResourceNotFoundException("This request has already been processed");
        }

        // Update request status
        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        // Create bidirectional friendship records
        Friends friendship1 = Friends.builder()
                .user(request.getSender())
                .friend(request.getReceiver())
                .build();

        Friends friendship2 = Friends.builder()
                .user(request.getReceiver())
                .friend(request.getSender())
                .build();

        friendsRepository.save(friendship1);
        friendsRepository.save(friendship2);

        log.info("Friend request {} accepted. Users {} and {} are now friends",
                requestId, request.getSender().getId(), request.getReceiver().getId());

        // Notify sender
        notificationService.createNotification(
                request.getSender(),
                currentUser,
                "FRIEND_ACCEPT",
                currentUser.getUsername() + " accepted your friend request",
                String.valueOf(currentUser.getId()));

        return toFriendRequestDTO(request);
    }

    @Transactional
    public ResGetFriendRequestDTO rejectFriendRequest(Long requestId)
            throws ResourceNotFoundException {

        User currentUser = getCurrentUser();
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with id = " + requestId));

        // Verify that the current user is the receiver
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("You can only reject requests sent to you");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResourceNotFoundException("This request has already been processed");
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        FriendRequest saved = friendRequestRepository.save(request);

        log.info("Friend request {} rejected by user {}", requestId, currentUser.getId());

        return toFriendRequestDTO(saved);
    }

    public List<ResGetFriendRequestDTO> getPendingRequests() {
        User currentUser = getCurrentUser();
        List<FriendRequest> requests = friendRequestRepository
                .findByReceiverAndStatusOrderByCreatedAtDesc(currentUser, FriendRequestStatus.PENDING);

        return requests.stream()
                .map(this::toFriendRequestDTO)
                .collect(Collectors.toList());
    }

    public List<ResGetFriendRequestDTO> getSentRequests() {
        User currentUser = getCurrentUser();
        List<FriendRequest> requests = friendRequestRepository
                .findBySenderAndStatusOrderByCreatedAtDesc(currentUser, FriendRequestStatus.PENDING);

        return requests.stream()
                .map(this::toFriendRequestDTO)
                .collect(Collectors.toList());
    }

    public List<ResGetFriendDTO> getFriends() {
        User currentUser = getCurrentUser();
        List<Friends> friendships = friendsRepository.findAllFriendsForUser(currentUser);

        List<ResGetFriendDTO> friends = new ArrayList<>();
        Set<Long> addedFriendIds = new HashSet<>();

        for (Friends friendship : friendships) {
            // Get the other user in the friendship
            User friend = friendship.getUser().getId().equals(currentUser.getId())
                    ? friendship.getFriend()
                    : friendship.getUser();

            // Skip if we've already added this friend (deduplication)
            if (addedFriendIds.contains(friend.getId())) {
                continue;
            }

            ResGetFriendDTO dto = ResGetFriendDTO.builder()
                    .id(friend.getId())
                    .username(friend.getUsername())
                    .email(friend.getEmail())
                    .avatarUrl(friend.getAvatarUrl())
                    .bio(friend.getBio())
                    .createdAt(friend.getCreatedAt())
                    .friendsSince(friendship.getCreatedAt())
                    .build();

            friends.add(dto);
            addedFriendIds.add(friend.getId());
        }

        return friends;
    }

    @Transactional
    public void unfriend(Long friendId) throws ResourceNotFoundException {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("Current user not found");
        }

        User friendUser = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = " + friendId));

        // Check if they are actually friends
        if (!friendsRepository.areFriends(currentUser, friendUser)) {
            throw new ResourceNotFoundException("You are not friends with this user");
        }

        // Find and delete all friendship records between the two users (both
        // directions)
        List<Friends> friendships = friendsRepository.findFriendshipBetweenUsers(currentUser, friendUser);
        if (!friendships.isEmpty()) {
            friendsRepository.deleteAll(friendships);
            log.info("Unfriended: User {} and User {} are no longer friends", currentUser.getId(), friendId);
        }
    }

    private ResGetFriendRequestDTO toFriendRequestDTO(FriendRequest request) {
        return ResGetFriendRequestDTO.builder()
                .id(request.getId())
                .senderId(request.getSender().getId())
                .senderUsername(request.getSender().getUsername())
                .senderEmail(request.getSender().getEmail())
                .senderAvatarUrl(request.getSender().getAvatarUrl())
                .receiverId(request.getReceiver().getId())
                .receiverUsername(request.getReceiver().getUsername())
                .receiverEmail(request.getReceiver().getEmail())
                .receiverAvatarUrl(request.getReceiver().getAvatarUrl())
                .status(request.getStatus().toString())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
