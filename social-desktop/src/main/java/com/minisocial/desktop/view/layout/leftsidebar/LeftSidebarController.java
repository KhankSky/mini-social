package com.minisocial.desktop.view.layout.leftsidebar;

import com.minisocial.desktop.UserSession;
import com.minisocial.desktop.view.layout.main.MainLayoutController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LeftSidebarController {
    private final UserSession session;
    private final MainLayoutController mainController;

    @FXML
    private Label fullNameLabel;
    @FXML
    private Label usernameLabel;

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
}
