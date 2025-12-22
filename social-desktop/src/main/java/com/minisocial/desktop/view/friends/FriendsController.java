package com.minisocial.desktop.view.friends;

import com.minisocial.desktop.AppNavigator;
import com.minisocial.desktop.UserSession;
import com.minisocial.desktop.dto.FriendDTO;
import com.minisocial.desktop.dto.FriendRequestDTO;
import com.minisocial.desktop.dto.UserDTO;
import com.minisocial.desktop.service.FriendService;
import com.minisocial.desktop.service.UserService;
import com.minisocial.desktop.view.layout.main.MainLayoutController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class FriendsController {
    private final UserSession session;
    private final FriendService friendService;
    private final UserService userService;
    private final MainLayoutController mainLayout;

    @FXML
    private ToggleButton friendsTab;
    @FXML
    private ToggleButton requestsTab;
    @FXML
    private ToggleButton findTab;
    @FXML
    private VBox itemsList;
    @FXML
    private VBox loadingView;
    @FXML
    private VBox emptyView;
    @FXML
    private Label emptyEmoji;
    @FXML
    private Label emptyTitle;
    @FXML
    private Label emptySubtitle;

    private ToggleGroup tabGroup;

    public FriendsController(AppNavigator navigator, UserSession session, MainLayoutController mainLayout) {
        this.session = session;
        this.friendService = new FriendService();
        this.userService = new UserService();
        this.mainLayout = mainLayout;
    }

    @FXML
    private void initialize() {
        tabGroup = new ToggleGroup();
        friendsTab.setToggleGroup(tabGroup);
        requestsTab.setToggleGroup(tabGroup);
        findTab.setToggleGroup(tabGroup);

        loadFriends();
    }

    @FXML
    private void handleTabChange() {
        if (friendsTab.isSelected())
            loadFriends();
        else if (requestsTab.isSelected())
            loadRequests();
        else if (findTab.isSelected())
            loadFindFriends();
    }

    private void loadFriends() {
        showLoading(true);
        new Thread(() -> {
            try {
                List<FriendDTO> friends = friendService.getFriends();
                Platform.runLater(() -> {
                    showLoading(false);
                    itemsList.getChildren().clear();
                    if (friends.isEmpty()) {
                        showEmpty("😊", "No friends yet", "Start by finding and adding friends!");
                    } else {
                        hideEmpty();
                        for (FriendDTO friend : friends) {
                            itemsList.getChildren().add(createFriendCard(friend));
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to load friends", e));
            }
        }).start();
    }

    private void loadRequests() {
        showLoading(true);
        new Thread(() -> {
            try {
                List<FriendRequestDTO> requests = friendService.getPendingRequests();
                Platform.runLater(() -> {
                    showLoading(false);
                    itemsList.getChildren().clear();
                    if (requests.isEmpty()) {
                        showEmpty("📭", "No pending requests", "You're all caught up!");
                    } else {
                        hideEmpty();
                        for (FriendRequestDTO req : requests) {
                            itemsList.getChildren().add(createRequestCard(req));
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to load requests", e));
            }
        }).start();
    }

    private void loadFindFriends() {
        showLoading(true);
        new Thread(() -> {
            try {
                // Here we usually call a search or suggested users endpoint
                // For now, let's use a generic user fetch
                List<UserDTO> users = userService.getAllUsers(1, 20).getResult();
                Platform.runLater(() -> {
                    showLoading(false);
                    itemsList.getChildren().clear();
                    // Filter out self and potentially existing friends
                    for (UserDTO user : users) {
                        if (!user.getId().equals(session.getUserId())) {
                            itemsList.getChildren().add(createUserSearchCard(user));
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to load users", e));
            }
        }).start();
    }

    private HBox createFriendCard(FriendDTO friend) {
        HBox card = createBaseCard(friend.getUsername(), friend.getEmail());

        HBox actions = new HBox(8);

        Button messageBtn = new Button("Message");
        messageBtn.setStyle(
                "-fx-background-color: #e0e7ff; -fx-text-fill: #6366f1; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        messageBtn.setOnAction(e -> mainLayout.showMessages(friend.getId()));

        Button unfriendBtn = new Button("Unfriend");
        unfriendBtn.setStyle(
                "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        unfriendBtn.setOnAction(e -> handleUnfriend(friend));

        actions.getChildren().addAll(messageBtn, unfriendBtn);
        card.getChildren().add(actions);
        return card;
    }

    private HBox createRequestCard(FriendRequestDTO req) {
        HBox card = createBaseCard(req.getSenderUsername(), req.getSenderEmail());

        HBox actions = new HBox(8);
        Button acceptBtn = new Button("Accept");
        acceptBtn.setStyle(
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        acceptBtn.setOnAction(e -> handleAccept(req));

        Button rejectBtn = new Button("Reject");
        rejectBtn.setStyle(
                "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        rejectBtn.setOnAction(e -> handleReject(req));

        actions.getChildren().addAll(acceptBtn, rejectBtn);
        card.getChildren().add(actions);
        return card;
    }

    private HBox createUserSearchCard(UserDTO user) {
        HBox card = createBaseCard(user.getUsername(), user.getEmail());

        Button addBtn = new Button("Add Friend");

        String status = user.getRelationStatus();
        boolean disabled = false;

        if ("PENDING_SENT".equals(status)) {
            addBtn.setText("Sent");
            disabled = true;
        } else if ("FRIEND".equals(status)) {
            addBtn.setText("Friend");
            disabled = true;
        } else if ("SELF".equals(status)) {
            addBtn.setText("You");
            disabled = true;
        } else if ("PENDING_RECEIVED".equals(status)) {
            addBtn.setText("Confirm");
        }

        addBtn.setDisable(disabled);
        addBtn.setStyle(
                "-fx-background-color: " + (disabled ? "#f3f4f6" : "#e0e7ff") + "; " +
                        "-fx-text-fill: " + (disabled ? "#9ca3af" : "#6366f1") + "; " +
                        "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: "
                        + (disabled ? "default" : "hand") + ";");

        addBtn.setOnAction(e -> handleAddFriend(user));

        card.getChildren().add(addBtn);
        return card;
    }

    private HBox createBaseCard(String name, String meta) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 5, 0, 0, 2);");

        Circle avatar = new Circle(24, Color.web("#e5e7eb"));
        VBox info = new VBox(4);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label metaLabel = new Label(meta);
        metaLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

        info.getChildren().addAll(nameLabel, metaLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        card.getChildren().addAll(avatar, info);
        return card;
    }

    private void handleUnfriend(FriendDTO friend) {
        new Thread(() -> {
            try {
                friendService.unfriend(friend.getId());
                Platform.runLater(this::loadFriends);
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to unfriend", e));
            }
        }).start();
    }

    private void handleAccept(FriendRequestDTO req) {
        new Thread(() -> {
            try {
                friendService.acceptRequest(req.getId());
                Platform.runLater(this::loadRequests);
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to accept request", e));
            }
        }).start();
    }

    private void handleReject(FriendRequestDTO req) {
        new Thread(() -> {
            try {
                friendService.rejectRequest(req.getId());
                Platform.runLater(this::loadRequests);
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to reject request", e));
            }
        }).start();
    }

    private void handleAddFriend(UserDTO user) {
        new Thread(() -> {
            try {
                friendService.sendRequest(user.getId());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("Friend request sent to " + user.getUsername());
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to send request", e));
            }
        }).start();
    }

    private void showLoading(boolean loading) {
        loadingView.setVisible(loading);
        loadingView.setManaged(loading);
        itemsList.setVisible(!loading);
        itemsList.setManaged(!loading);
        if (loading)
            hideEmpty();
    }

    private void showEmpty(String emoji, String title, String subtitle) {
        emptyEmoji.setText(emoji);
        emptyTitle.setText(title);
        emptySubtitle.setText(subtitle);
        emptyView.setVisible(true);
        emptyView.setManaged(true);
    }

    private void hideEmpty() {
        emptyView.setVisible(false);
        emptyView.setManaged(false);
    }

    private void showError(String message, Exception e) {
        showLoading(false);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
