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

        // Simulate quick auth and move to feed. Replace with real API later.
        Platform.runLater(() -> {
            session.setUsername(username);
            session.setFullName(username);
            setLoading(false);
            navigator.showFeed();
        });
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
