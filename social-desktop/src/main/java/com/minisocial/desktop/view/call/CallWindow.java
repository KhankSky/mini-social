package com.minisocial.desktop.view.call;

import com.minisocial.desktop.config.AppConfig;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CallWindow {
    private final String toUsername;
    private final boolean isVideo;
    private final String type; // 'offer' or 'answer'
    private final String sdp;

    public CallWindow(String toUsername, boolean isVideo, String type, String sdp) {
        this.toUsername = toUsername;
        this.isVideo = isVideo;
        this.type = type;
        this.sdp = sdp;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle((isVideo ? "Video" : "Voice") + " Call with " + toUsername);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // Construct URL with parameters
        String url = AppConfig.SERVER_BASE_URL.replace("/api", "") + "/call-window"
                + "?to=" + URLEncoder.encode(toUsername, StandardCharsets.UTF_8)
                + "&type=" + type
                + "&video=" + isVideo;

        if (sdp != null) {
            url += "&sdp=" + URLEncoder.encode(sdp, StandardCharsets.UTF_8);
        }

        System.out.println("Opening Call Window: " + url);
        engine.load(url);

        // Optional: Handle window close Bridge to end call if needed
        stage.setOnCloseRequest(event -> {
            engine.executeScript("if (window.handleEndCall) window.handleEndCall();");
        });

        Scene scene = new Scene(new StackPane(webView), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
