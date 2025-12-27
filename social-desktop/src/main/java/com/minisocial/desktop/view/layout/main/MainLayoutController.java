package com.minisocial.desktop.view.layout.main;

import com.minisocial.desktop.AppNavigator;
import com.minisocial.desktop.UserSession;
import com.minisocial.desktop.view.feed.FeedController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

import com.minisocial.desktop.view.feed.FeedController;
import com.minisocial.desktop.view.layout.leftsidebar.LeftSidebarController;

public class MainLayoutController {
    private final AppNavigator navigator;
    private final UserSession session;
    private LeftSidebarController leftSidebarController;
    private FeedController feedController;

    @FXML
    private BorderPane mainLayout;

    public MainLayoutController(AppNavigator navigator, UserSession session) {
        this.navigator = navigator;
        this.session = session;
    }

    @FXML
    private void initialize() {
        try {
            URL leftUrl = MainLayoutController.class.getResource("/view/layout/leftsidebar/left_sidebar.fxml");
            FXMLLoader leftLoader = new FXMLLoader(leftUrl);
            leftLoader.setControllerFactory(
                    param -> {
                        leftSidebarController = new com.minisocial.desktop.view.layout.leftsidebar.LeftSidebarController(session, this);
                        return leftSidebarController;
                    });
            VBox leftSidebar = leftLoader.load();
            mainLayout.setLeft(leftSidebar);

            URL rightUrl = MainLayoutController.class.getResource("/view/layout/rightsidebar/right_sidebar.fxml");
            FXMLLoader rightLoader = new FXMLLoader(rightUrl);
            rightLoader.setControllerFactory(
                    param -> new com.minisocial.desktop.view.layout.rightsidebar.RightSidebarController());
            VBox rightSidebar = rightLoader.load();
            mainLayout.setRight(rightSidebar);

            // Default view is Feed
            showFeed();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load layout components: " + e.getMessage(), e);
        }
    }

    public void showFeed() {
        loadView("/view/feed/feed.fxml", param -> {
            feedController = new FeedController(navigator, session);
            return feedController;
        });
    }

    public void showMessages() {
        showMessages(null);
    }

    public void showMessages(Long targetUserId) {
        loadView("/view/messages/messages.fxml",
                param -> new com.minisocial.desktop.view.messages.MessagesController(navigator, session, targetUserId));
    }

    public void showFriends() {
        loadView("/view/friends/friends.fxml",
                param -> new com.minisocial.desktop.view.friends.FriendsController(navigator, session, this));
    }

    public void showNotifications() {
        loadView("/view/notifications/notifications.fxml",
                param -> {
                    com.minisocial.desktop.view.notifications.NotificationsController controller =
                        new com.minisocial.desktop.view.notifications.NotificationsController();
                    controller.setAppNavigator(navigator);
                    return controller;
                });
    }

    public void logout() {
        session.clear();
        navigator.showLogin();
    }

    public void showSearch() {
        loadView("/view/search/search.fxml",
                param -> new com.minisocial.desktop.view.search.SearchController());
    }

    public void showSettings() {
        loadView("/view/settings/settings.fxml",
                param -> new com.minisocial.desktop.view.settings.SettingsController(session, this));
    }

    public void showAdmin() {
        loadView("/view/admin/admin.fxml",
                param -> new com.minisocial.desktop.view.admin.AdminController());
    }

    public void updateAllAvatars() {
        if (leftSidebarController != null) {
            leftSidebarController.updateUserAvatar();
        }
        if (feedController != null) {
            feedController.updateUserAvatar();
        }
    }

    public LeftSidebarController getLeftSidebarController() {
        return leftSidebarController;
    }

    private void loadView(String fxmlPath, javafx.util.Callback<Class<?>, Object> controllerFactory) {
        try {
            URL url = MainLayoutController.class.getResource(fxmlPath);
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(controllerFactory);
            mainLayout.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
