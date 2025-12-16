package com.minisocial.desktop.view.layout.leftsidebar;

import com.minisocial.desktop.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LeftSidebarController {
    private final UserSession session;

    @FXML
    private Label fullNameLabel;
    @FXML
    private Label usernameLabel;

    public LeftSidebarController(UserSession session) {
        this.session = session;
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
}
