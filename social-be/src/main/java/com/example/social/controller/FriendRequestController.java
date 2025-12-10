package com.example.social.controller;

import com.example.social.controller.error.ResourceAlreadyExistsException;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.dto.request.friend.ReqSendFriendRequestDTO;
import com.example.social.dto.response.friend.ResGetFriendDTO;
import com.example.social.dto.response.friend.ResGetFriendRequestDTO;
import com.example.social.service.FriendRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    public FriendRequestController(FriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;
    }

    // Send a friend request
    @PostMapping("/requests")
    public ResponseEntity<ResGetFriendRequestDTO> sendFriendRequest(
            @Valid @RequestBody ReqSendFriendRequestDTO request) 
            throws ResourceNotFoundException, ResourceAlreadyExistsException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendRequestService.sendFriendRequest(request));
    }

    // Accept a friend request
    @PutMapping("/requests/{id}/accept")
    public ResponseEntity<ResGetFriendRequestDTO> acceptFriendRequest(@PathVariable Long id) 
            throws ResourceNotFoundException {
        return ResponseEntity.ok(friendRequestService.acceptFriendRequest(id));
    }

    // Reject a friend request
    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<ResGetFriendRequestDTO> rejectFriendRequest(@PathVariable Long id) 
            throws ResourceNotFoundException {
        return ResponseEntity.ok(friendRequestService.rejectFriendRequest(id));
    }

    // Get pending friend requests (received)
    @GetMapping("/requests/pending")
    public ResponseEntity<List<ResGetFriendRequestDTO>> getPendingRequests() {
        return ResponseEntity.ok(friendRequestService.getPendingRequests());
    }

    // Get sent friend requests
    @GetMapping("/requests/sent")
    public ResponseEntity<List<ResGetFriendRequestDTO>> getSentRequests() {
        return ResponseEntity.ok(friendRequestService.getSentRequests());
    }

    // Get friends list
    @GetMapping
    public ResponseEntity<List<ResGetFriendDTO>> getFriends() {
        return ResponseEntity.ok(friendRequestService.getFriends());
    }

    // Unfriend a user
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> unfriend(@PathVariable Long friendId) 
            throws ResourceNotFoundException {
        friendRequestService.unfriend(friendId);
        return ResponseEntity.ok().build();
    }
}
