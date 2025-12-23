package com.minisocial.desktop.view.notifications;

import com.minisocial.desktop.service.WebSocketService;
import com.minisocial.desktop.service.NotificationPopupService;
import com.minisocial.desktop.service.SystemTrayNotificationService;
import com.minisocial.desktop.config.AppConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class NotificationsController {

    @FXML
    private ListView<Map<String, Object>> notificationListView;

    private final WebSocketService webSocketService = new WebSocketService();
    private final NotificationPopupService popupService = new NotificationPopupService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public NotificationsController() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @FXML
    public void initialize() {
        setupListView();
        loadNotifications();
        connectWebSocket();
    }

    private void setupListView() {
        notificationListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox container = new HBox(10);
                    container.setPadding(new Insets(10));
                    boolean isRead = (boolean) item.get("read");
                    if (!isRead) {
                        container.setStyle("-fx-background-color: #EEF2FF;");
                    }

                    VBox textContainer = new VBox(5);
                    String actorName = (String) item.get("actorName");
                    String type = (String) item.get("type");
                    String content = (String) item.get("content");

                    Text title = new Text(actorName + " " + getActionText(type));
                    title.setStyle("-fx-font-weight: bold;");

                    Text subtitle = new Text(content != null ? content : "");
                    subtitle.setFill(Color.GRAY);

                    textContainer.getChildren().addAll(title, subtitle);
                    container.getChildren().add(textContainer);

                    setGraphic(container);
                }
            }
        });
    }

    private String getActionText(String type) {
        if ("COMMENT".equals(type))
            return "commented on your post";
        if ("FRIEND_REQUEST".equals(type))
            return "sent you a friend request";
        if ("FRIEND_ACCEPT".equals(type))
            return "accepted your friend request";
        return "sent a notification";
    }

    private void loadNotifications() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/notifications"))
                        .header("Authorization", AppConfig.AUTH_TOKEN)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    List<Map<String, Object>> notifications = objectMapper.readValue(response.body(),
                            new TypeReference<>() {
                            });
                    Platform.runLater(() -> notificationListView.getItems().setAll(notifications));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void connectWebSocket() {
        webSocketService.connect(
                msg -> {
                },
                call -> {
                },
                notification -> {
                    Map<String, Object> notifMap = (Map<String, Object>) notification;
                    Platform.runLater(() -> {
                        // Add to list view
                        notificationListView.getItems().add(0, notifMap);

                        // Show popup notification
                        popupService.showNotification(notifMap);

                        // Show system tray notification
                        SystemTrayNotificationService.showNotification(notifMap);
                    });
                });
    }

    @FXML
    private void handleMarkAllRead() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/notifications/read-all"))
                        .header("Authorization", AppConfig.AUTH_TOKEN)
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build();

                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                loadNotifications(); // Reload to update UI
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
