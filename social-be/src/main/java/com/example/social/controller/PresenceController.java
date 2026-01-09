package com.example.social.controller;

import com.example.social.domain.User;
import com.example.social.dto.response.user.ResGetUserDTO;
import com.example.social.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final UserPresenceService userPresenceService;

    @GetMapping("/online")
    public ResponseEntity<List<ResGetUserDTO>> getOnlineUsers() {
        log.debug("Getting online users");
        List<User> onlineUsers = userPresenceService.getOnlineUsers();

        List<ResGetUserDTO> response = onlineUsers.stream()
                .map(user -> {
                    ResGetUserDTO dto = new ResGetUserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setAvatarUrl(user.getAvatarUrl());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getOnlineUserCount() {
        return ResponseEntity.ok(userPresenceService.getOnlineUserCount());
    }
}
