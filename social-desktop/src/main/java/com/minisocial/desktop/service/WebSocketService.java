package com.minisocial.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minisocial.desktop.config.AppConfig;
import com.minisocial.desktop.dto.MessageDTO;
import javafx.application.Platform;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WebSocketService {
    private StompSession session;
    private final String url = AppConfig.SERVER_BASE_URL + "/ws";
    private Consumer<MessageDTO> onMessageReceived;
    private Consumer<Map<String, Object>> onCallReceived;
    private Consumer<Object> onNotificationReceived; // Using Object or a proper DTO

    public void connect(Consumer<MessageDTO> onMessageReceived,
            Consumer<Map<String, Object>> onCallReceived,
            Consumer<Object> onNotificationReceived) {
        this.onMessageReceived = onMessageReceived;
        this.onCallReceived = onCallReceived;
        this.onNotificationReceived = onNotificationReceived;

        // Use SockJS for better compatibility
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient socketClient = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(socketClient);

        // Check Jackson configuration for Java 8 Time (LocalDateTime)
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Authorization", AppConfig.AUTH_TOKEN);

        StompHeaders connectHeaders = new StompHeaders();
        // Send Authorization header (Bearer token)
        // connectHeaders.add("Authorization", AppConfig.AUTH_TOKEN);

        System.out.println("Connecting to WebSocket at: " + url);
        stompClient.connectAsync(url, handshakeHeaders, connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("WebSocket Connected! Session ID: " + session.getSessionId());
                WebSocketService.this.session = session;
                subscribeToMessages();
                subscribeToCalls();
                subscribeToNotifications();
            }

            @Override
            public void handleException(StompSession session,
                    org.springframework.messaging.simp.stomp.StompCommand command, StompHeaders headers, byte[] payload,
                    Throwable exception) {
                System.err.println("WebSocket Exception: " + exception.getMessage());
                exception.printStackTrace();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("WebSocket Transport Error: " + exception.getMessage());
                // Handle reconnection logic via UI or service manager if needed
            }
        });
    }

    private void subscribeToMessages() {
        if (session == null || !session.isConnected())
            return;

        System.out.println("Subscribing to /user/queue/messages");
        session.subscribe("/user/queue/messages", new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof MessageDTO) {
                    MessageDTO message = (MessageDTO) payload;
                    System.out.println("Received message via WebSocket: " + message.getContent());
                    if (onMessageReceived != null) {
                        Platform.runLater(() -> onMessageReceived.accept(message));
                    }
                }
            }
        });
    }

    private void subscribeToCalls() {
        if (session == null || !session.isConnected())
            return;

        System.out.println("Subscribing to /user/queue/call");
        session.subscribe("/user/queue/call", new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> callData = (Map<String, Object>) payload;
                    System.out.println("Received call signal via WebSocket: " + callData.get("type"));
                    if (onCallReceived != null) {
                        Platform.runLater(() -> onCallReceived.accept(callData));
                    }
                }
            }
        });
    }

    private void subscribeToNotifications() {
        if (session == null || !session.isConnected())
            return;

        System.out.println("Subscribing to /user/queue/notifications");
        session.subscribe("/user/queue/notifications", new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                // We can use Map or a DTO. Let's use Map for simplicity or duplicate DTO.
                // Since DTO is in backend, we should create one in desktop or use Map.
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof Map) {
                    System.out.println("Received notification via WebSocket");
                    if (onNotificationReceived != null) {
                        Platform.runLater(() -> onNotificationReceived.accept(payload));
                    }
                }
            }
        });
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("WebSocket Disconnected");
        }
    }
}
