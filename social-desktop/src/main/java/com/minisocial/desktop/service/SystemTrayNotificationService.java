package com.minisocial.desktop.service;

import javafx.application.Platform;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.util.Map;

public class SystemTrayNotificationService {

    private static SystemTray tray;
    private static TrayIcon trayIcon;

    public static void initialize() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported on this platform");
            return;
        }

        try {
            tray = SystemTray.getSystemTray();

            // Create tray icon (use a simple image or icon)
            // For simplicity, using a small colored square
            Image image = Toolkit.getDefaultToolkit().createImage(new byte[0]);

            trayIcon = new TrayIcon(image, "Mini Social");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Mini Social Desktop");

            // Add to system tray
            tray.add(trayIcon);
        } catch (Exception e) {
            System.err.println("Failed to initialize system tray: " + e.getMessage());
        }
    }

    public static void showNotification(Map<String, Object> notification) {
        if (trayIcon == null) {
            return;
        }

        Platform.runLater(() -> {
            String actorName = (String) notification.get("actorName");
            String type = (String) notification.get("type");
            String message = getActionText(type);

            trayIcon.displayMessage(
                    actorName,
                    message,
                    MessageType.INFO);
        });
    }

    private static String getActionText(String type) {
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

    public static void remove() {
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
        }
    }
}
