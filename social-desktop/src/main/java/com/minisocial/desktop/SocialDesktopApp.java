package com.minisocial.desktop;

import com.minisocial.desktop.service.SystemTrayNotificationService;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SocialDesktopApp extends Application {
    private AppNavigator navigator;

    @Override
    public void start(Stage stage) {
        // Initialize system tray notifications
        SystemTrayNotificationService.initialize();

        UserSession session = new UserSession();
        navigator = new AppNavigator(stage, session);
        navigator.showLogin();
        stage.setTitle("MiniSocial Desktop");
        stage.getIcons().add(new Image(
                getClass().getResourceAsStream("/images/logo.png")));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
