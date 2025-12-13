/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.minisocial.desktop.view.feed;

import com.minisocial.desktop.AppNavigator;
import com.minisocial.desktop.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class FeedController {
    private final AppNavigator navigator;
    private final UserSession session;
    private final ObservableList<Post> posts = FXCollections.observableArrayList();
    private final Image defaultAvatar = new Image("https://api.dicebear.com/7.x/avataaars/png?seed=User");

    @FXML
    private TextArea composeText;
    @FXML
    private VBox postsContainer;
    @FXML
    private Button publishButton;
    @FXML
    private ScrollPane scrollPane;

    public FeedController(AppNavigator navigator, UserSession session) {
        this.navigator = navigator;
        this.session = session;
    }

    @FXML
    private void initialize() {
        seedPosts();
        renderPosts();
        scrollPane.setFitToWidth(true);
    }

    @FXML
    private void handlePublish() {
        String content = composeText.getText() != null ? composeText.getText().trim() : "";
        if (content.isEmpty()) {
            return;
        }
        Post newPost = new Post(session.getFullName(), "@" + session.getUsername(), content, "Just now", "Hanoi, Vietnam");
        posts.add(0, newPost);
        composeText.clear();
        renderPosts();
    }

    private void seedPosts() {
        posts.setAll(List.of(
                new Post("Bogdan Nikitin", "@nikitinteam", "Exploring the mountains today and the view is unbelievable!", "2h ago", "Dalat, Vietnam"),
                new Post("Brittni Lando", "@brit.lando", "Morning coffee hits different when the sun is out ☕️", "3h ago", "Ho Chi Minh City"),
                new Post("Ivan Shevchenko", "@ivan.shev", "Weekend sprint review done. Shipping new features soon!", "5h ago", "Kyiv, Ukraine")
        ));
    }

    private void renderPosts() {
        postsContainer.getChildren().clear();
        for (Post post : posts) {
            postsContainer.getChildren().add(buildPostCard(post));
        }
    }

private VBox buildPostCard(Post post) {

        // Avatar
        ImageView avatar = new ImageView(defaultAvatar);
        avatar.setFitWidth(48);
        avatar.setFitHeight(48);
        avatar.setClip(new Circle(24, 24, 24));

        // Author
        Label name = new Label(post.author);
        name.getStyleClass().add("post-author");

        Label username = new Label(post.username);
        username.getStyleClass().add("post-username");

        VBox authorInfo = new VBox(2, name, username);
        HBox authorBox = new HBox(8, avatar, authorInfo);
        authorBox.setAlignment(Pos.CENTER_LEFT);

        // Meta (time + location)
        FontIcon locationIcon = new FontIcon("fas-map-marker-alt");

        Label timeLabel = new Label(post.timeAgo);
        Label dot = new Label("•");
        Label locationLabel = new Label(post.location);

        HBox metaBox = new HBox(6, timeLabel, dot, locationIcon, locationLabel);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        metaBox.getStyleClass().add("post-meta");

        // Content
        Label content = new Label(post.content);
        content.setWrapText(true);
        content.getStyleClass().add("post-content");

        // Actions
        HBox actions = new HBox(16,
                pillButton("fas-heart", "Like"),
                pillButton("fas-comment", "Comment"),
                pillButton("fas-share", "Share")
        );
        actions.setAlignment(Pos.CENTER_LEFT);

        // Card
        VBox card = new VBox(12, authorBox, metaBox, content, actions);
        card.getStyleClass().add("post-card");
        card.setPadding(new Insets(16));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinWidth(Region.USE_COMPUTED_SIZE);

        return card;
    }

    private Button pillButton(String iconLiteral, String text) {

        FontIcon icon = new FontIcon(iconLiteral);

        Label label = new Label(text);
        label.getStyleClass().add("button-label");

        HBox graphic = new HBox(6, icon, label);
        graphic.setAlignment(Pos.CENTER);

        Button button = new Button();
        button.setGraphic(graphic);
        button.getStyleClass().add("pill-button");

        return button;
    }

    private record Post(String author, String username, String content, String timeAgo, String location) { }
}
