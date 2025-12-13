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

public class MainLayoutController {
    private final AppNavigator navigator;
    private final UserSession session;

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
            leftLoader.setControllerFactory(param -> new com.minisocial.desktop.view.layout.leftsidebar.LeftSidebarController(session));
            VBox leftSidebar = leftLoader.load();
            mainLayout.setLeft(leftSidebar);

            URL rightUrl = MainLayoutController.class.getResource("/view/layout/rightsidebar/right_sidebar.fxml");
            FXMLLoader rightLoader = new FXMLLoader(rightUrl);
            rightLoader.setControllerFactory(param -> new com.minisocial.desktop.view.layout.rightsidebar.RightSidebarController());
            VBox rightSidebar = rightLoader.load();
            mainLayout.setRight(rightSidebar);

            URL feedUrl = MainLayoutController.class.getResource("/view/feed/feed.fxml");
            FXMLLoader feedLoader = new FXMLLoader(feedUrl);
            feedLoader.setControllerFactory(param -> new FeedController(navigator, session));
            mainLayout.setCenter(feedLoader.load());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load layout components: " + e.getMessage(), e);
        }
    }
}
