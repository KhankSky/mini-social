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
import java.util.concurrent.CopyOnWriteArrayList;

public class WebSocketService {
    // Singleton instance
    private static WebSocketService instance;
    
    private StompSession session;
    private final String url = AppConfig.SERVER_BASE_URL + "/ws";
    
    // Store multiple listeners for different components
    private final List<Consumer<MessageDTO>> messageListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Map<String, Object>>> callListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Object>> notificationListeners = new CopyOnWriteArrayList<>();
    
    private boolean isConnecting = false;
    private boolean isConnected = false;

    // Private constructor for singleton
    private WebSocketService() {
    }
    
    // Get singleton instance
    public static synchronized WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    public void addMessageListener(Consumer<MessageDTO> listener) {
        if (!messageListeners.contains(listener)) {
            messageListeners.add(listener);
        }
    }
    
    public void removeMessageListener(Consumer<MessageDTO> listener) {
        messageListeners.remove(listener);
    }
    
    public void addCallListener(Consumer<Map<String, Object>> listener) {
        if (!callListeners.contains(listener)) {
            callListeners.add(listener);
        }
    }
    
    public void removeCallListener(Consumer<Map<String, Object>> listener) {
        callListeners.remove(listener);
    }
    
    public void addNotificationListener(Consumer<Object> listener) {
        if (!notificationListeners.contains(listener)) {
            notificationListeners.add(listener);
        }
    }
    
    public void removeNotificationListener(Consumer<Object> listener) {
        notificationListeners.remove(listener);
    }

    public synchronized void connect(Consumer<MessageDTO> onMessageReceived,
            Consumer<Map<String, Object>> onCallReceived,
            Consumer<Object> onNotificationReceived) {
        
        // Add listeners
        if (onMessageReceived != null) addMessageListener(onMessageReceived);
        if (onCallReceived != null) addCallListener(onCallReceived);
        if (onNotificationReceived != null) addNotificationListener(onNotificationReceived);
        
        // If already connected or connecting, don't reconnect
        if (isConnected || isConnecting) {
            System.out.println("WebSocket already connected or connecting");
            return;
        }
        
        isConnecting = true;

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

        // Extract token without "Bearer " prefix for URL parameter
        String token = AppConfig.AUTH_TOKEN;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // Build URL with token as query parameter (required for SockJS authentication)
        String wsUrl = url + "?token=" + token;

        System.out.println("Connecting to WebSocket at: " + wsUrl);
        stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("WebSocket Connected! Session ID: " + session.getSessionId());
                WebSocketService.this.session = session;
                isConnected = true;
                isConnecting = false;
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
                isConnecting = false;
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("WebSocket Transport Error: " + exception.getMessage());
                isConnected = false;
                isConnecting = false;
                // Auto-reconnect after 5 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        reconnect();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }
    
    private void reconnect() {
        System.out.println("Attempting to reconnect WebSocket...");
        isConnected = false;
        isConnecting = false;
        connect(null, null, null);
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
                    // Notify all listeners
                    for (Consumer<MessageDTO> listener : messageListeners) {
                        Platform.runLater(() -> listener.accept(message));
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
                    // Notify all listeners
                    for (Consumer<Map<String, Object>> listener : callListeners) {
                        Platform.runLater(() -> listener.accept(callData));
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
                    // Notify all listeners
                    for (Consumer<Object> listener : notificationListeners) {
                        Platform.runLater(() -> listener.accept(payload));
                    }
                }
            }
        });
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            isConnected = false;
            System.out.println("WebSocket Disconnected");
        }
    }
    
    public boolean isConnected() {
        return isConnected && session != null && session.isConnected();
    }
}
