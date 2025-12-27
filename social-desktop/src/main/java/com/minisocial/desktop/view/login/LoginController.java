/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.minisocial.desktop.view.login;

import com.minisocial.desktop.AppNavigator;
import com.minisocial.desktop.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class LoginController {
    private final AppNavigator navigator;
    private final UserSession session;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator spinner;
    @FXML
    private Button loginButton;

    public LoginController(AppNavigator navigator, UserSession session) {
        this.navigator = navigator;
        this.session = session;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        spinner.setVisible(false);
    }

    @FXML
    private void handleSignIn() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                com.minisocial.desktop.service.AuthService authService = new com.minisocial.desktop.service.AuthService();
                String token = authService.login(username, password);

                // Set the token globally
                com.minisocial.desktop.config.AppConfig.AUTH_TOKEN = "Bearer " + token;

                // Get user details to populate session
                java.util.Map<String, Object> userDetails = authService.getCurrentUserDetails(token);

                Platform.runLater(() -> {
                    if (userDetails != null) {
                        Object idObj = userDetails.get("id");
                        if (idObj instanceof Number idNum) {
                            session.setUserId(idNum.longValue());
                        } else if (idObj instanceof String idStr) {
                            try {
                                session.setUserId(Long.parseLong(idStr));
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        String usernameVal = (String) userDetails.getOrDefault("username", "");
                        session.setUsername(usernameVal);

                        String fullNameVal = (String) userDetails.getOrDefault("fullName", usernameVal);
                        session.setFullName(fullNameVal);

                        String avatarUrl = (String) userDetails.getOrDefault("avatarUrl", null);
                        session.setAvatarUrl(avatarUrl);
                    } else {
                        System.err.println("User details were null after successful login");
                    }
                    setLoading(false);
                    navigator.showFeed();
                });
            } catch (Exception e) {
                // Log exception for debugging
                System.err.println("Login error: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    setLoading(false);
                    String errorMsg = e.getMessage();
                    if (errorMsg == null || errorMsg.trim().isEmpty()) {
                        errorMsg = "An unexpected error occurred (" + e.getClass().getSimpleName() + ")";
                    }
                    showError("Login failed: " + errorMsg);
                });
            }
        }).start();
    }

    @FXML
    private void handleGoToRegister() {
        navigator.showRegister();
    }

    private void setLoading(boolean loading) {
        spinner.setVisible(loading);
        loginButton.setDisable(loading);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
