/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.minisocial.desktop.view.register;

import com.minisocial.desktop.AppNavigator;
import com.minisocial.desktop.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class RegisterController {
    private final AppNavigator navigator;
    private final UserSession session;

    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator spinner;
    @FXML
    private Button registerButton;

    public RegisterController(AppNavigator navigator, UserSession session) {
        this.navigator = navigator;
        this.session = session;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        spinner.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address.");
            return;
        }

        setLoading(true);

        // Simulate registration and move to login. Replace with real API later.
        Platform.runLater(() -> {
            // In real implementation, call API to register
            // For now, just simulate success and go to login
            setLoading(false);
            navigator.showLogin();
        });
    }

    @FXML
    private void handleBackToLogin() {
        navigator.showLogin();
    }

    private void setLoading(boolean loading) {
        spinner.setVisible(loading);
        registerButton.setDisable(loading);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
