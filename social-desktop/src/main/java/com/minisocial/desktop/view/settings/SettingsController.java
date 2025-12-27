package com.minisocial.desktop.view.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minisocial.desktop.config.AppConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import com.minisocial.desktop.view.layout.main.MainLayoutController;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.minisocial.desktop.UserSession;

public class SettingsController {
    private final UserSession session;
    private MainLayoutController mainLayoutController;

    @FXML
    private TextField usernameField;
    @FXML
    private TextArea bioArea;
    @FXML
    private TextField avatarUrlField;

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;

    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Long userId; // We need to know current user ID

    public SettingsController(UserSession session, MainLayoutController mainLayoutController) {
        this.session = session;
        this.mainLayoutController = mainLayoutController;
    }

    @FXML
    private VBox profileView;
    @FXML
    private VBox securityView;
    @FXML
    private Button profileBtn;
    @FXML
    private Button securityBtn;

    @FXML
    public void initialize() {
        // Load user profile
        loadUserProfile();
        // Default view
        handleShowProfile();
    }

    @FXML
    private void handleShowProfile() {
        if (profileView != null) {
            profileView.setVisible(true);
            profileView.setManaged(true);
            securityView.setVisible(false);
            securityView.setManaged(false);
            updateSidebarState(profileBtn);
        }
    }

    @FXML
    private void handleShowSecurity() {
        if (securityView != null) {
            profileView.setVisible(false);
            profileView.setManaged(false);
            securityView.setVisible(true);
            securityView.setManaged(true);
            updateSidebarState(securityBtn);
        }
    }

    private void updateSidebarState(Button active) {
        if (profileBtn == null || securityBtn == null)
            return;
        profileBtn.getStyleClass().remove("selected");
        securityBtn.getStyleClass().remove("selected");
        active.getStyleClass().add("selected");
    }

    private void loadUserProfile() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/auth/me"))
                        .header("Authorization", AppConfig.AUTH_TOKEN)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Map<String, Object> user = objectMapper.readValue(response.body(), Map.class);
                    userId = ((Number) user.get("id")).longValue();
                    Platform.runLater(() -> {
                        usernameField.setText((String) user.get("username"));
                        bioArea.setText((String) user.get("bio"));
                        avatarUrlField.setText((String) user.get("avatarUrl"));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleProfileSave() {
        if (userId == null)
            return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", userId);
        payload.put("username", usernameField.getText());
        payload.put("bio", bioArea.getText());
        payload.put("avatarUrl", avatarUrlField.getText());

        new Thread(() -> {
            try {
                String json = objectMapper.writeValueAsString(payload);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/users"))
                        .header("Authorization", AppConfig.AUTH_TOKEN)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        // Update the user session with the new avatar URL
                        String newAvatarUrl = avatarUrlField.getText();
                        session.setAvatarUrl(newAvatarUrl);

                        // Update avatars in all relevant UI components
                        if (mainLayoutController != null) {
                            mainLayoutController.updateAllAvatars();
                        }

                        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Network error."));
            }
        }).start();
    }

    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match.");
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("currentPassword", current);
        payload.put("newPassword", newPass);
        payload.put("confirmPassword", confirmPass);

        new Thread(() -> {
            try {
                String json = objectMapper.writeValueAsString(payload);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/users/password"))
                        .header("Authorization", AppConfig.AUTH_TOKEN)
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
                        currentPasswordField.clear();
                        newPasswordField.clear();
                        confirmPasswordField.clear();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to change password. Check current password.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Network error."));
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
