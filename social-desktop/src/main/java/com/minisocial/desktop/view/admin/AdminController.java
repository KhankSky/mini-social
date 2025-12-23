package com.minisocial.desktop.view.admin;

import com.minisocial.desktop.service.AdminService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminController {

    @FXML
    private Label userRoleLabel;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalPostsLabel;
    @FXML
    private Label totalCommentsLabel;
    @FXML
    private Label activeTodayLabel;

    @FXML
    private Button dashboardTab;
    @FXML
    private Button usersTab;
    @FXML
    private Button postsTab;

    @FXML
    private javafx.scene.layout.VBox dashboardView;
    @FXML
    private javafx.scene.layout.VBox usersView;
    @FXML
    private javafx.scene.layout.VBox postsView;

    @FXML
    private TextField userSearchField;
    @FXML
    private TableView<Map<String, Object>> usersTable;
    @FXML
    private TableView<Map<String, Object>> postsTable;

    private final AdminService adminService = new AdminService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // Set role badge (you should get this from current user)
        userRoleLabel.setText("ADMIN");

        setupUsersTable();
        setupPostsTable();
        loadStatistics();
        loadUsers();
    }

    @FXML
    private void showDashboard() {
        switchTab(dashboardTab, dashboardView);
        loadStatistics();
    }

    @FXML
    private void showUsers() {
        switchTab(usersTab, usersView);
        loadUsers();
    }

    @FXML
    private void showPosts() {
        switchTab(postsTab, postsView);
        loadPosts();
    }

    private void switchTab(Button activeButton, javafx.scene.layout.VBox activeView) {
        // Remove selected class from all tabs
        dashboardTab.getStyleClass().remove("selected");
        usersTab.getStyleClass().remove("selected");
        postsTab.getStyleClass().remove("selected");

        // Add selected class to active tab
        activeButton.getStyleClass().add("selected");

        // Hide all views
        dashboardView.setVisible(false);
        dashboardView.setManaged(false);
        usersView.setVisible(false);
        usersView.setManaged(false);
        postsView.setVisible(false);
        postsView.setManaged(false);

        // Show active view
        activeView.setVisible(true);
        activeView.setManaged(true);
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                Map<String, Object> stats = adminService.getStatistics();
                Platform.runLater(() -> {
                    totalUsersLabel.setText(String.valueOf(stats.get("totalUsers")));
                    totalPostsLabel.setText(String.valueOf(stats.get("totalPosts")));
                    totalCommentsLabel.setText(String.valueOf(stats.get("totalComments")));
                    activeTodayLabel.setText(String.valueOf(stats.get("activeToday")));
                });
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to load statistics: " + e.getMessage());
            }
        }).start();
    }

    private void setupUsersTable() {
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get("id"))));
        idCol.setPrefWidth(60);

        TableColumn<Map<String, Object>, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("username")));
        usernameCol.setPrefWidth(150);

        TableColumn<Map<String, Object>, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("email")));
        emailCol.setPrefWidth(200);

        TableColumn<Map<String, Object>, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("role")));
        roleCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(data -> {
            String dateStr = (String) data.getValue().get("createdAt");
            return new SimpleStringProperty(dateStr != null ? dateStr.substring(0, 10) : "");
        });
        createdCol.setPrefWidth(150);

        TableColumn<Map<String, Object>, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(event -> {
                    Map<String, Object> user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        actionsCol.setPrefWidth(120);

        usersTable.getColumns().clear();
        usersTable.getColumns().addAll(idCol, usernameCol, emailCol, roleCol, createdCol, actionsCol);
    }

    private void setupPostsTable() {
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get("id"))));
        idCol.setPrefWidth(60);

        TableColumn<Map<String, Object>, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> {
            Map<String, Object> user = (Map<String, Object>) data.getValue().get("user");
            return new SimpleStringProperty(user != null ? (String) user.get("username") : "Unknown");
        });
        authorCol.setPrefWidth(150);

        TableColumn<Map<String, Object>, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("content")));
        contentCol.setPrefWidth(400);

        TableColumn<Map<String, Object>, String> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(data -> {
            String dateStr = (String) data.getValue().get("createdAt");
            return new SimpleStringProperty(dateStr != null ? dateStr.substring(0, 16).replace('T', ' ') : "");
        });
        createdCol.setPrefWidth(150);

        TableColumn<Map<String, Object>, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(event -> {
                    Map<String, Object> post = getTableView().getItems().get(getIndex());
                    handleDeletePost(post);
                });
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        actionsCol.setPrefWidth(100);

        postsTable.getColumns().clear();
        postsTable.getColumns().addAll(idCol, authorCol, contentCol, createdCol, actionsCol);
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                String search = userSearchField != null ? userSearchField.getText() : "";
                List<Map<String, Object>> users = adminService.getAllUsers(0, 50, search);
                ObservableList<Map<String, Object>> items = FXCollections.observableArrayList(users);
                Platform.runLater(() -> usersTable.setItems(items));
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to load users: " + e.getMessage());
            }
        }).start();
    }

    private void loadPosts() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> posts = adminService.getAllPosts(0, 50);
                ObservableList<Map<String, Object>> items = FXCollections.observableArrayList(posts);
                Platform.runLater(() -> postsTable.setItems(items));
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to load posts: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleUserSearch() {
        loadUsers();
    }

    private void handleDeleteUser(Map<String, Object> user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Delete user: " + user.get("username"));
        confirm.setContentText("Are you sure? This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Long userId = Long.valueOf(String.valueOf(user.get("id")));
                        adminService.deleteUser(userId);
                        Platform.runLater(() -> {
                            showInfo("User deleted successfully");
                            loadUsers();
                        });
                    } catch (Exception e) {
                        showError("Failed to delete user: " + e.getMessage());
                    }
                }).start();
            }
        });
    }

    private void handleDeletePost(Map<String, Object> post) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Post");
        confirm.setHeaderText("Delete this post?");
        confirm.setContentText("This will also delete all comments. Continue?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Long postId = Long.valueOf(String.valueOf(post.get("id")));
                        adminService.deletePost(postId);
                        Platform.runLater(() -> {
                            showInfo("Post deleted successfully");
                            loadPosts();
                        });
                    } catch (Exception e) {
                        showError("Failed to delete post: " + e.getMessage());
                    }
                }).start();
            }
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
