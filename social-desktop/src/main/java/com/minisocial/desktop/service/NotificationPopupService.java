package com.minisocial.desktop.service;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import java.io.InputStream;
import java.util.Map;

public class NotificationPopupService {

    private static final int NOTIFICATION_WIDTH = 350;
    private static final int NOTIFICATION_HEIGHT = 100;
    private static final int MARGIN = 20;

    public void showNotification(Map<String, Object> notification) {
        Platform.runLater(() -> {
            Stage popupStage = new Stage();
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.setAlwaysOnTop(true);

            // Create notification content
            VBox container = new VBox(5);
            container.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);" +
                            "-fx-padding: 15;");
            container.setPrefSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);

            // Header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            // Avatar
            String actorName = (String) notification.get("actorName");
            String actorAvatar = (String) notification.get("actorAvatar");
            ImageView avatar = new ImageView();
            avatar.setFitWidth(40);
            avatar.setFitHeight(40);
            avatar.setPreserveRatio(true);

            // Load avatar or use default
            try {
                if (actorAvatar != null && !actorAvatar.isEmpty()) {
                    avatar.setImage(new Image(actorAvatar, true));
                } else {
                    avatar.setImage(new Image("https://ui-avatars.com/api/?name=" + actorName));
                }
            } catch (Exception e) {
                // Use default if loading fails
                avatar.setImage(new Image("https://ui-avatars.com/api/?name=" + actorName));
            }

            // Set circular clip for avatar
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20, 20, 20);
            avatar.setClip(clip);

            // Content
            VBox contentBox = new VBox(2);
            Label nameLabel = new Label(actorName);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            String type = (String) notification.get("type");
            String actionText = getActionText(type);
            Label actionLabel = new Label(actionText);
            actionLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            contentBox.getChildren().addAll(nameLabel, actionLabel);

            // Close button
            Label closeBtn = new Label("✕");
            closeBtn.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #999;");
            closeBtn.setOnMouseClicked(e -> popupStage.close());

            header.getChildren().addAll(avatar, contentBox, closeBtn);
            HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);

            container.getChildren().add(header);

            // Create scene
            Scene scene = new Scene(container);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            popupStage.setScene(scene);

            // Position at bottom-right corner
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            popupStage.setX(screenBounds.getMaxX() - NOTIFICATION_WIDTH - MARGIN);
            popupStage.setY(screenBounds.getMaxY() - NOTIFICATION_HEIGHT - MARGIN);

            // Show with fade-in animation
            popupStage.setOpacity(0);
            popupStage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Auto-close after 5 seconds
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> popupStage.close());
                fadeOut.play();
            });
            delay.play();

            // Click to close
            container.setOnMouseClicked(e -> popupStage.close());

            // Play notification sound
            playNotificationSound();
        });
    }

    private String getActionText(String type) {
        if ("COMMENT".equals(type))
            return "commented on your post";
        if ("FRIEND_REQUEST".equals(type))
            return "sent you a friend request";
        if ("FRIEND_ACCEPT".equals(type))
            return "accepted your friend request";
        if ("MESSAGE".equals(type))
            return "sent you a message";
        return "sent a notification";
    }

    private void playNotificationSound() {
        new Thread(() -> {
            try {
                InputStream soundStream = getClass().getResourceAsStream("/sounds/notification.wav");
                if (soundStream != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                }
            } catch (Exception e) {
                System.err.println("Could not play notification sound: " + e.getMessage());
            }
        }).start();
    }
}
