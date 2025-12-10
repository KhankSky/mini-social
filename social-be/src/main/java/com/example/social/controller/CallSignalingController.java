package com.example.social.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CallSignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/call.offer")
    public void handleCallOffer(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String toUsername = (String) payload.get("to");
        String fromUsername = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        log.info("Call offer from {} to {}", fromUsername, toUsername);

        payload.put("from", fromUsername);
        messagingTemplate.convertAndSendToUser(toUsername, "/queue/call", payload);
    }

    @MessageMapping("/call.answer")
    public void handleCallAnswer(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String toUsername = (String) payload.get("to");
        String fromUsername = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        log.info("Call answer from {} to {}", fromUsername, toUsername);

        payload.put("from", fromUsername);
        messagingTemplate.convertAndSendToUser(toUsername, "/queue/call", payload);
    }

    @MessageMapping("/call.ice-candidate")
    public void handleIceCandidate(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String toUsername = (String) payload.get("to");
        String fromUsername = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        log.debug("ICE candidate from {} to {}", fromUsername, toUsername);

        payload.put("from", fromUsername);
        messagingTemplate.convertAndSendToUser(toUsername, "/queue/call", payload);
    }

    @MessageMapping("/call.reject")
    public void handleCallReject(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String toUsername = (String) payload.get("to");
        String fromUsername = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        log.info("Call rejected from {} to {}", fromUsername, toUsername);

        payload.put("type", "CALL_REJECT");
        payload.put("from", fromUsername);
        messagingTemplate.convertAndSendToUser(toUsername, "/queue/call", payload);
    }

    @MessageMapping("/call.end")
    public void handleCallEnd(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String toUsername = (String) payload.get("to");
        String fromUsername = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        log.info("Call ended from {} to {}", fromUsername, toUsername);

        payload.put("type", "CALL_END");
        payload.put("from", fromUsername);
        messagingTemplate.convertAndSendToUser(toUsername, "/queue/call", payload);
    }
}
