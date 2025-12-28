package com.example.social.service;

import com.example.social.domain.User;
import com.example.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final UserRepository userRepository;

    // Map of username -> set of session IDs (supports multiple sessions per user)
    private final Map<String, Set<String>> onlineUsers = new ConcurrentHashMap<>();

    public void userConnected(String username, String sessionId) {
        log.info("User connected: {} with session {}", username, sessionId);
        onlineUsers.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void userDisconnected(String sessionId) {
        log.info("Session disconnected: {}", sessionId);
        // Find and remove the session
        onlineUsers.values().forEach(sessions -> sessions.remove(sessionId));
        // Clean up users with no sessions
        onlineUsers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public List<User> getOnlineUsers() {
        Set<String> onlineUsernames = onlineUsers.keySet();
        log.debug("Getting online users: {}", onlineUsernames);

        if (onlineUsernames.isEmpty()) {
            return Collections.emptyList();
        }

        return userRepository.findAll().stream()
                .filter(user -> onlineUsernames.contains(user.getUsername()))
                .collect(Collectors.toList());
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.containsKey(username) && !onlineUsers.get(username).isEmpty();
    }

    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
}
