package com.minisocial.desktop;


import com.minisocial.desktop.view.login.LoginController;
import com.minisocial.desktop.view.register.RegisterController;
import com.minisocial.desktop.view.layout.main.MainLayoutController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class AppNavigator {
    private final Stage stage;
    private final UserSession session;
    private final List<String> stylesheets;
    private MainLayoutController mainLayoutController;

    public AppNavigator(Stage stage, UserSession session) {
        this.stage = stage;
        this.session = session;
        this.stylesheets = Arrays.asList(
            "/css/global.css",
            "/css/login.css",
            "/css/sidebar.css",
            "/css/feed.css"
        );
    }

    public void showLogin() {
        loadScene("/view/login/login.fxml", loader -> new LoginController(this, session), 
                  Arrays.asList("/css/global.css", "/css/login.css"));
    }

    public void showRegister() {
        loadScene("/view/register/register.fxml", loader -> new RegisterController(this, session), 
                  Arrays.asList("/css/global.css", "/css/login.css"));
    }

    public void showFeed() {
        loadScene("/view/layout/main/main_layout.fxml",
                  loader -> {
                      mainLayoutController = new MainLayoutController(this, session);
                      return mainLayoutController;
                  },
                  stylesheets);
    }

    public MainLayoutController getCurrentMainLayoutController() {
        return mainLayoutController;
    }

    private void loadScene(String fxmlPath, javafx.util.Callback<Class<?>, Object> controllerFactory,
                          List<String> cssFiles) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Load CSS files
            for (String cssPath : cssFiles) {
                URL css = getClass().getResource(cssPath);
                if (css != null) {
                    scene.getStylesheets().add(css.toExternalForm());
                }
            }

            stage.setScene(scene);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load view: " + fxmlPath, e);
        }
    }
}
