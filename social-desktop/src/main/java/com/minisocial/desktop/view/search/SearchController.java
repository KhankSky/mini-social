package com.minisocial.desktop.view.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minisocial.desktop.config.AppConfig;
import com.minisocial.desktop.dto.FriendDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SearchController {

    @FXML
    private TextField searchField;
    @FXML
    private ListView<Map<String, Object>> userListView;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Timer debounceTimer;

    @FXML
    public void initialize() {
        setupSearchField();
        setupListView();
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (debounceTimer != null) {
                debounceTimer.cancel();
            }
            debounceTimer = new Timer();
            debounceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> performSearch(newValue));
                }
            }, 500);
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            userListView.getItems().clear();
            return;
        }

        new Thread(() -> {
            try {
                // spring-filter: fullName~~'%query%' or username~~'%query%'
                String filter = String.format("fullName~~'%%%s%%' or username~~'%%%s%%'", query, query);
                String encodedFilter = URLEncoder.encode(filter, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/users?filter=" + encodedFilter + "&size=10"))
                        .header("Authorization", AppConfig.AUTH_TOKEN)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {
                    });
                    List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("result");
                    Platform.runLater(() -> userListView.getItems().setAll(users));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupListView() {
        userListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(12);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setPadding(new Insets(8));

                    // Avatar
                    String avatarUrl = (String) item.get("avatarUrl");
                    if (avatarUrl == null)
                        avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=" + item.get("username");

                    if (avatarUrl.startsWith("/"))
                        avatarUrl = AppConfig.SERVER_BASE_URL + avatarUrl;

                    ImageView avatar = new ImageView();
                    try {
                        avatar.setImage(new Image(avatarUrl, true));
                    } catch (Exception e) {
                        // Fallback
                    }
                    avatar.setFitWidth(40);
                    avatar.setFitHeight(40);
                    Circle clip = new Circle(20, 20, 20);
                    avatar.setClip(clip);

                    // Name info
                    VBox info = new VBox(2);
                    String name = (String) item.getOrDefault("fullName", item.get("username"));
                    Label nameLabel = new Label(name);
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label usernameLabel = new Label("@" + item.get("username"));
                    usernameLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

                    info.getChildren().addAll(nameLabel, usernameLabel);

                    container.getChildren().addAll(avatar, info);
                    setGraphic(container);
                }
            }
        });
    }
}
