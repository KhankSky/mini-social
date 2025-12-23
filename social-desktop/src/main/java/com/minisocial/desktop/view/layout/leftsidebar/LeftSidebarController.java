package com.minisocial.desktop.view.layout.leftsidebar;

import com.minisocial.desktop.UserSession;
import com.minisocial.desktop.view.layout.main.MainLayoutController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class LeftSidebarController {
    private final UserSession session;
    private final MainLayoutController mainController;

    @FXML
    private Label fullNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button adminButton;

    public LeftSidebarController(UserSession session, MainLayoutController mainController) {
        this.session = session;
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        if (fullNameLabel != null) {
            fullNameLabel.setText(session.getFullName());
        }
        if (usernameLabel != null) {
            usernameLabel.setText("@" + session.getUsername());
        }

        // Show admin button only for admin/moderator
        if (adminButton != null) {
            String role = session.getRole();
            adminButton.setVisible(role != null && (role.equals("ADMIN") || role.equals("MODERATOR")));
            adminButton.setManaged(role != null && (role.equals("ADMIN") || role.equals("MODERATOR")));
        }
    }

    @FXML
    private void handleFeed() {
        mainController.showFeed();
    }

    @FXML
    private void handleMessages() {
        mainController.showMessages();
    }

    @FXML
    private void handleFriends() {
        mainController.showFriends();
    }

    @FXML
    private void handleNotifications() {
        mainController.showNotifications();
    }

    @FXML
    private void handleLogout() {
        mainController.logout();
    }

    @FXML
    private void handleSearch() {
        mainController.showSearch();
    }

    @FXML
    private void handleSettings() {
        mainController.showSettings();
    }

    @FXML
    private void handleAdmin() {
        mainController.showAdmin();
    }
}
