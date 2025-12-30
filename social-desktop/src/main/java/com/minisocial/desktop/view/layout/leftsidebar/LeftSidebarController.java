package com.minisocial.desktop.view.layout.leftsidebar;

import com.minisocial.desktop.UserSession;
import com.minisocial.desktop.view.layout.main.MainLayoutController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.application.Platform;
import java.util.Map;
import com.minisocial.desktop.service.WebSocketService;
import com.minisocial.desktop.config.AppConfig;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;



public class LeftSidebarController {
    private final UserSession session;
    private final MainLayoutController mainController;

    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button adminButton;
    @FXML
    private Circle notificationIndicator;

    // Navigation buttons
    @FXML
    private Button feedButton;
    @FXML
    private Button messagesButton;
    @FXML
    private Button notificationsButton;
    @FXML
    private Button friendsButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;

    private Button activeButton;

    private final WebSocketService webSocketService = WebSocketService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    // Store listener reference for cleanup
    private Consumer<Object> notificationListener;

    public LeftSidebarController(UserSession session, MainLayoutController mainController) {
        this.session = session;
        this.mainController = mainController;
        objectMapper.registerModule(new JavaTimeModule());
    }

    @FXML
    private void initialize() {
        if (fullNameLabel != null) {
            fullNameLabel.setText(session.getFullName());
        }
        if (usernameLabel != null) {
            usernameLabel.setText("@" + session.getUsername());
        }

        // Load user avatar
        if (avatarImageView != null) {
            String avatarUrl = session.getAvatarUrl();
            Image defaultAvatar = new Image("https://api.dicebear.com/7.x/avataaars/png?seed=" + session.getUsername());
            Image avatarImage = defaultAvatar;

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                try {
                    avatarImage = new Image(avatarUrl, true);
                    avatarImage.errorProperty().addListener((obs, oldError, newError) -> {
                        if (newError) {
                            // If avatar fails to load, use default avatar
                            Platform.runLater(() -> avatarImageView.setImage(defaultAvatar));
                        }
                    });
                } catch (Exception e) {
                    // If there's an exception creating the image, use default avatar
                    avatarImage = defaultAvatar;
                }
            }

            avatarImageView.setImage(avatarImage);
            avatarImageView.setFitWidth(56);
            avatarImageView.setFitHeight(56);
            avatarImageView.setPreserveRatio(true);
            avatarImageView.setSmooth(true);
            avatarImageView.setPickOnBounds(true);

            // Create circular clip
            Circle clip = new Circle(28, 28, 28);
            avatarImageView.setClip(clip);
        }

        // Show admin button only for admin/moderator
        if (adminButton != null) {
            String role = session.getRole();
            adminButton.setVisible(role != null && (role.equals("ADMIN") || role.equals("MODERATOR")));
            adminButton.setManaged(role != null && (role.equals("ADMIN") || role.equals("MODERATOR")));
        }

        // Initialize notification indicator
        if (notificationIndicator != null) {
            notificationIndicator.setVisible(false);
        }

        // Load unread notifications count and connect to WebSocket
        loadUnreadNotificationsCount();
        connectWebSocket();

        // Set the initial active button (News Feed) - this will handle the styling
        setActiveButton(feedButton);
    }

    @FXML
    private void handleFeed() {
        mainController.showFeed();
        setActiveButton(feedButton);
    }

    @FXML
    private void handleMessages() {
        mainController.showMessages();
        setActiveButton(messagesButton);
    }

    @FXML
    private void handleFriends() {
        mainController.showFriends();
        setActiveButton(friendsButton);
    }

    @FXML
    private void handleNotifications() {
        mainController.showNotifications();
        setActiveButton(notificationsButton);
    }

    @FXML
    private void handleLogout() {
        mainController.logout();
        setActiveButton(logoutButton);
    }

    @FXML
    private void handleSearch() {
        mainController.showSearch();
        setActiveButton(searchButton);
    }

    @FXML
    private void handleSettings() {
        mainController.showSettings();
        setActiveButton(settingsButton);
    }

    @FXML
    private void handleAdmin() {
        mainController.showAdmin();
        setActiveButton(adminButton);
    }

    private void setActiveButton(Button newActiveButton) {
        // Remove active styling from the previous active button
        if (activeButton != null) {
            activeButton.getStyleClass().remove("primary-nav");
            // Set the icon color back to the default dark color for non-active buttons
            if (activeButton == feedButton) {
                // For the newspaper icon in feed button
                setIconColor(activeButton, "#1f2937");
            } else {
                // For other buttons, find their icon and set appropriate color
                setIconColor(activeButton, "#1f2937");
            }
        }

        // Set the new active button
        activeButton = newActiveButton;

        // Add active styling to the new active button
        if (activeButton != null) {
            if (!activeButton.getStyleClass().contains("primary-nav")) {
                activeButton.getStyleClass().add("primary-nav");
            }

            // Set the icon color to white for the active button
            setIconColor(activeButton, "white");
        }
    }

    private void setIconColor(Button button, String color) {
        if (button.getGraphic() instanceof HBox) {
            HBox outerHBox = (HBox) button.getGraphic();
            if (outerHBox.getChildren().size() > 0 && outerHBox.getChildren().get(0) instanceof HBox) {
                HBox innerHBox = (HBox) outerHBox.getChildren().get(0);
                if (innerHBox.getChildren().size() > 0) {
                    javafx.scene.Node firstChild = innerHBox.getChildren().get(0);
                    if (firstChild instanceof FontIcon) {
                        FontIcon fontIcon = (FontIcon) firstChild;
                        fontIcon.setIconColor(Color.web(color));
                    }
                }
            }
        }
    }

    public void updateUserAvatar() {
        if (avatarImageView != null) {
            String avatarUrl = session.getAvatarUrl();
            Image defaultAvatar = new Image("https://api.dicebear.com/7.x/avataaars/png?seed=" + session.getUsername());
            Image avatarImage = defaultAvatar;

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                try {
                    avatarImage = new Image(avatarUrl, true);
                    avatarImage.errorProperty().addListener((obs, oldError, newError) -> {
                        if (newError) {
                            // If avatar fails to load, use default avatar
                            Platform.runLater(() -> avatarImageView.setImage(defaultAvatar));
                        }
                    });
                } catch (Exception e) {
                    // If there's an exception creating the image, use default avatar
                    avatarImage = defaultAvatar;
                }
            }

            avatarImageView.setImage(avatarImage);
            avatarImageView.setFitWidth(56);
            avatarImageView.setFitHeight(56);
            avatarImageView.setPreserveRatio(true);
            avatarImageView.setSmooth(true);
            avatarImageView.setPickOnBounds(true);

            // Create circular clip
            Circle clip = new Circle(28, 28, 28);
            avatarImageView.setClip(clip);
        }
    }

    private void loadUnreadNotificationsCount() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/notifications/unread-count"))
                        .header("Authorization", AppConfig.AUTH_TOKEN)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    int unreadCount = Integer.parseInt(response.body());
                    Platform.runLater(() -> {
                        if (notificationIndicator != null) {
                            notificationIndicator.setVisible(unreadCount > 0);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void connectWebSocket() {
        // Save listener reference for cleanup
        notificationListener = notification -> {
            // When a new notification arrives, update the indicator
            Platform.runLater(() -> {
                if (notificationIndicator != null) {
                    notificationIndicator.setVisible(true);
                }
            });
        };
        
        webSocketService.connect(null, null, notificationListener);
    }
    
    public void cleanup() {
        // Remove listener when controller is destroyed
        if (notificationListener != null) {
            webSocketService.removeNotificationListener(notificationListener);
        }
    }

    public void hideNotificationIndicator() {
        Platform.runLater(() -> {
            if (notificationIndicator != null) {
                notificationIndicator.setVisible(false);
            }
        });
    }
}
