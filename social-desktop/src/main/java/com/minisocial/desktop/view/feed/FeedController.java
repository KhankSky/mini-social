package com.minisocial.desktop.view.feed;

import com.minisocial.desktop.AppNavigator;
import com.minisocial.desktop.UserSession;
import com.minisocial.desktop.config.AppConfig;
import com.minisocial.desktop.dto.*;
import com.minisocial.desktop.service.CommentService;
import com.minisocial.desktop.service.LocationService;
import com.minisocial.desktop.service.PostService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FeedController {
    private final AppNavigator navigator;
    private final UserSession session;
    private final PostService postService;
    private final CommentService commentService;
    private final LocationService locationService;
    private final ObservableList<PostDTO> posts = FXCollections.observableArrayList();
    private final Image defaultAvatar = new Image("https://api.dicebear.com/7.x/avataaars/png?seed=User");

    @FXML private TextArea composeText;
    @FXML private VBox postsContainer;
    @FXML private Button publishButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Button uploadImageButton;
    @FXML private HBox selectedImagesBox;
    @FXML private HBox locationContainer;
    @FXML private TextField locationField;
    @FXML private Button searchLocationButton;
    @FXML private Button clearLocationButton;
    @FXML private VBox locationResultsBox;

    private List<File> selectedImages = new ArrayList<>();
    private LocationDTO selectedLocation = null;
    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLoading = false;

    public FeedController(AppNavigator navigator, UserSession session) {
        this.navigator = navigator;
        this.session = session;
        this.postService = new PostService();
        this.commentService = new CommentService();
        this.locationService = new LocationService();
    }

    @FXML
    private void initialize() {
        scrollPane.setFitToWidth(true);
        setupLocationAutocomplete();
        loadPosts();
    }

    private void setupLocationAutocomplete() {
        locationField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.trim().length() >= 2) {
                searchLocationsAsync(newVal.trim());
            } else {
                hideLocationResults();
            }
        });
    }

    @FXML
    private void handleAddLocation() {
        locationContainer.setManaged(true);
        locationContainer.setVisible(true);
        locationField.requestFocus();
    }

    @FXML
    private void handleSearchLocation() {
        String query = locationField.getText();
        if (query != null && !query.trim().isEmpty()) {
            searchLocationsAsync(query.trim());
        }
    }

    @FXML
    private void handleClearLocation() {
        selectedLocation = null;
        locationField.clear();
        locationContainer.setManaged(false);
        locationContainer.setVisible(false);
        hideLocationResults();
    }

    private void searchLocationsAsync(String query) {
        new Thread(() -> {
            try {
                List<LocationDTO> locations = locationService.searchLocations(query, 5);
                Platform.runLater(() -> displayLocationResults(locations));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    hideLocationResults();
                    showAlert("Error", "Failed to search locations: " + e.getMessage());
                });
            }
        }).start();
    }

    private void displayLocationResults(List<LocationDTO> locations) {
        locationResultsBox.getChildren().clear();
        
        if (locations == null || locations.isEmpty()) {
            Label noResults = new Label("No locations found");
            noResults.getStyleClass().add("location-result-item");
            locationResultsBox.getChildren().add(noResults);
        } else {
            for (LocationDTO location : locations) {
                Button locationBtn = createLocationResultButton(location);
                locationResultsBox.getChildren().add(locationBtn);
            }
        }
        
        locationResultsBox.setManaged(true);
        locationResultsBox.setVisible(true);
    }

    private Button createLocationResultButton(LocationDTO location) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("location-result-item");
        
        FontIcon icon = new FontIcon("fas-map-marker-alt");
        icon.setIconSize(14);
        icon.setIconColor(javafx.scene.paint.Color.web("#6366f1"));
        
        Label nameLabel = new Label(location.getShortName() != null ? 
                location.getShortName() : location.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        
        Label displayLabel = new Label(location.getDisplayName());
        displayLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        displayLabel.setWrapText(true);
        
        VBox textBox = new VBox(2, nameLabel, displayLabel);
        HBox content = new HBox(8, icon, textBox);
        content.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphic(content);
        
        btn.setOnAction(e -> selectLocation(location));
        
        return btn;
    }

    private void selectLocation(LocationDTO location) {
        selectedLocation = location;
        locationField.setText(location.getShortName() != null ? 
                location.getShortName() : location.getName());
        hideLocationResults();
    }

    private void hideLocationResults() {
        locationResultsBox.getChildren().clear();
        locationResultsBox.setManaged(false);
        locationResultsBox.setVisible(false);
    }

    @FXML
    private void handleAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Images");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(uploadImageButton.getScene().getWindow());
        if (files != null) {
            selectedImages.addAll(files);
            updateSelectedImagesUI();
        }
    }

    private void updateSelectedImagesUI() {
        selectedImagesBox.getChildren().clear();
        
        if (selectedImages.isEmpty()) {
            selectedImagesBox.setManaged(false);
            selectedImagesBox.setVisible(false);
            return;
        }
        
        selectedImagesBox.setManaged(true);
        selectedImagesBox.setVisible(true);
        
        for (File file : selectedImages) {
            HBox imageItemBox = new HBox(8);
            imageItemBox.setAlignment(Pos.CENTER_LEFT);
            imageItemBox.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-padding: 8;");
            
            FontIcon imageIcon = new FontIcon("fas-image");
            imageIcon.setIconSize(20);
            imageIcon.setIconColor(javafx.scene.paint.Color.web("#6366f1"));
            
            Label nameLabel = new Label(file.getName());
            nameLabel.setStyle("-fx-font-size: 13px;");
            
            long sizeInKB = file.length() / 1024;
            Label sizeLabel = new Label(String.format("(%d KB)", sizeInKB));
            sizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Button removeBtn = new Button();
            FontIcon removeIcon = new FontIcon("fas-times");
            removeIcon.setIconSize(14);
            removeIcon.setIconColor(javafx.scene.paint.Color.web("#ef4444"));
            removeBtn.setGraphic(removeIcon);
            removeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                selectedImages.remove(file);
                updateSelectedImagesUI();
            });
            
            imageItemBox.getChildren().addAll(imageIcon, nameLabel, sizeLabel, spacer, removeBtn);
            selectedImagesBox.getChildren().add(imageItemBox);
        }
    }

    @FXML
    private void handlePublish() {
        String content = composeText.getText() != null ? composeText.getText().trim() : "";
        
        if (content.isEmpty()) {
            showAlert("Error", "Please enter some content for your post.");
            return;
        }

        String locationStr = selectedLocation != null ? 
                (selectedLocation.getShortName() != null ? selectedLocation.getShortName() : selectedLocation.getName()) : 
                null;

        publishButton.setDisable(true);
        publishButton.setText("Publishing...");

        final List<File> imagesToUpload = new ArrayList<>(selectedImages);

        new Thread(() -> {
            try {
                CreatePostRequest request = new CreatePostRequest(content, "PUBLIC", locationStr);
                PostDTO newPost = postService.createPost(request, imagesToUpload);

                Platform.runLater(() -> {
                    posts.add(0, newPost);
                    composeText.clear();
                    handleClearLocation();
                    selectedImages.clear();
                    updateSelectedImagesUI();
                    renderPosts();
                    publishButton.setDisable(false);
                    publishButton.setText("Publish");
                    showAlert("Success", "Post published successfully!");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    publishButton.setDisable(false);
                    publishButton.setText("Publish");
                    showAlert("Error", "Failed to publish post: " + e.getMessage());
                });
            }
        }).start();
    }

    private void loadPosts() {
        if (isLoading) return;
        isLoading = true;

        Label loadingLabel = new Label("Loading posts...");
        loadingLabel.getStyleClass().add("loading-label");
        postsContainer.getChildren().clear();
        postsContainer.getChildren().add(loadingLabel);

        new Thread(() -> {
            try {
                PaginationDTO<PostDTO> result = postService.getAllPosts(currentPage, pageSize);

                Platform.runLater(() -> {
                    posts.clear();
                    posts.addAll(result.getResult());
                    renderPosts();
                    isLoading = false;
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load posts: " + e.getMessage());
                    isLoading = false;
                    postsContainer.getChildren().clear();
                });
            }
        }).start();
    }

    private void renderPosts() {
        postsContainer.getChildren().clear();
        for (PostDTO post : posts) {
            postsContainer.getChildren().add(buildPostCard(post));
        }
    }

    private VBox buildPostCard(PostDTO post) {
        Image avatarImage = defaultAvatar;
        if (post.getUserAvatarUrl() != null && !post.getUserAvatarUrl().isEmpty()) {
            try {
                avatarImage = new Image(post.getUserAvatarUrl(), true);
            } catch (Exception e) {
                avatarImage = defaultAvatar;
            }
        }
        
        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(48);
        avatar.setFitHeight(48);
        avatar.setClip(new Circle(24, 24, 24));

        Label name = new Label(post.getUsername());
        name.getStyleClass().add("post-author");

        Label username = new Label("@" + post.getUsername());
        username.getStyleClass().add("post-username");

        VBox authorInfo = new VBox(2, name, username);
        HBox authorBox = new HBox(8, avatar, authorInfo);
        authorBox.setAlignment(Pos.CENTER_LEFT);

        String timeAgo = formatTimeAgo(post.getCreatedAt());
        Label timeLabel = new Label(timeAgo);
        
        HBox metaBox = new HBox(6, timeLabel);
        
        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            FontIcon locationIcon = new FontIcon("fas-map-marker-alt");
            locationIcon.setIconSize(12);
            locationIcon.setIconColor(javafx.scene.paint.Color.web("#6366f1"));
            Label dot = new Label("â€¢");
            Label locationLabel = new Label(post.getLocation());
            metaBox.getChildren().addAll(dot, locationIcon, locationLabel);
        }
        
        metaBox.setAlignment(Pos.CENTER_LEFT);
        metaBox.getStyleClass().add("post-meta");

        Label content = new Label(post.getContent());
        content.setWrapText(true);
        content.getStyleClass().add("post-content");
        VBox cardContent = new VBox(12);
        cardContent.getChildren().addAll(authorBox, metaBox, content);
        
        if (post.getAttachments() != null && !post.getAttachments().isEmpty()) {
            VBox imagesBox = buildPostImagesView(post.getAttachments());
            cardContent.getChildren().add(imagesBox);
        }

        Button likeBtn = pillButton("fas-heart", "Like (" + (post.getLikeCount() != null ? post.getLikeCount() : 0) + ")");
        Button commentBtn = pillButton("fas-comment", "Comment (" + (post.getCommentCount() != null ? post.getCommentCount() : 0) + ")");
        Button shareBtn = pillButton("fas-share", "Share");
        
        commentBtn.setOnAction(e -> showCommentsDialog(post));
        
        HBox actions = new HBox(16, likeBtn, commentBtn, shareBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        
        cardContent.getChildren().add(actions);

        // Card
        VBox card = new VBox();
        card.getChildren().add(cardContent);
        card.getStyleClass().add("post-card");
        card.setPadding(new Insets(16));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinWidth(Region.USE_COMPUTED_SIZE);

        return card;
    }

    private VBox buildPostImagesView(List<PostDTO.AttachmentDTO> attachments) {
        VBox imagesBox = new VBox(8);
        imagesBox.getStyleClass().add("post-images-container");
        List<PostDTO.AttachmentDTO> imageAttachments = attachments.stream()
                .filter(att -> att.getFileType() != null && att.getFileType().startsWith("image/"))
                .toList();
        
        if (imageAttachments.isEmpty()) {
            return imagesBox;
        }
        
        if (imageAttachments.size() == 1) {
            ImageView imageView = createPostImageView(imageAttachments.get(0).getFileUrl(), 600, 400);
            imagesBox.getChildren().add(imageView);
        } else if (imageAttachments.size() == 2) {
            HBox row = new HBox(8);
            for (PostDTO.AttachmentDTO att : imageAttachments) {
                ImageView imageView = createPostImageView(att.getFileUrl(), 290, 290);
                row.getChildren().add(imageView);
            }
            imagesBox.getChildren().add(row);
        } else {
            int cols = 2;
            HBox currentRow = null;
            for (int i = 0; i < imageAttachments.size(); i++) {
                if (i % cols == 0) {
                    currentRow = new HBox(8);
                    imagesBox.getChildren().add(currentRow);
                }
                ImageView imageView = createPostImageView(imageAttachments.get(i).getFileUrl(), 290, 290);
                currentRow.getChildren().add(imageView);
            }
        }
        
        return imagesBox;
    }

    private ImageView createPostImageView(String imageUrl, double maxWidth, double maxHeight) {
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(maxWidth);
        imageView.setFitHeight(maxHeight);
        imageView.setStyle("-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);");
        
        try {
            String fullImageUrl = AppConfig.getFullImageUrl(imageUrl);
            
            if (fullImageUrl != null) {
                System.out.println("Loading image from: " + fullImageUrl);
                
                Image image = new Image(fullImageUrl, true);
                imageView.setImage(image);
                
                image.errorProperty().addListener((obs, oldError, newError) -> {
                    if (newError) {
                        System.err.println("Failed to load image: " + fullImageUrl);
                    }
                });
                
                imageView.setOnMouseClicked(e -> {
                    System.out.println("Image clicked: " + fullImageUrl);
                });
                imageView.setStyle(imageView.getStyle() + "-fx-cursor: hand;");
            } else {
                System.err.println("Image URL is null or empty");
            }
        } catch (Exception e) {
            System.err.println("Exception loading image: " + imageUrl);
            e.printStackTrace();
        }
        
        return imageView;
    }

    private void showCommentsDialog(PostDTO post) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Comments");
        dialog.setHeaderText("Comments for post by @" + post.getUsername());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(500);
        content.setPrefHeight(400);

        ScrollPane scrollPane = new ScrollPane();
        VBox commentsContainer = new VBox(8);
        scrollPane.setContent(commentsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        Label loadingLabel = new Label("Loading comments...");
        commentsContainer.getChildren().add(loadingLabel);

        new Thread(() -> {
            try {
                List<CommentDTO> comments = commentService.getCommentsByPost(post.getId());
                Platform.runLater(() -> {
                    commentsContainer.getChildren().clear();
                    if (comments.isEmpty()) {
                        commentsContainer.getChildren().add(new Label("No comments yet. Be the first to comment!"));
                    } else {
                        for (CommentDTO comment : comments) {
                            commentsContainer.getChildren().add(buildCommentView(comment));
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    commentsContainer.getChildren().clear();
                    commentsContainer.getChildren().add(new Label("Failed to load comments: " + e.getMessage()));
                });
            }
        }).start();

        TextArea commentInput = new TextArea();
        commentInput.setPromptText("Write a comment...");
        commentInput.setPrefRowCount(2);

        Button submitBtn = new Button("Submit");
        submitBtn.setOnAction(e -> {
            String commentText = commentInput.getText().trim();
            if (!commentText.isEmpty()) {
                submitBtn.setDisable(true);
                new Thread(() -> {
                    try {
                        CreateCommentRequest request = new CreateCommentRequest(commentText);
                        CommentDTO newComment = commentService.createComment(post.getId(), request);
                        
                        Platform.runLater(() -> {
                            commentInput.clear();
                            commentsContainer.getChildren().add(0, buildCommentView(newComment));
                            submitBtn.setDisable(false);
                            post.setCommentCount((post.getCommentCount() != null ? post.getCommentCount() : 0) + 1);
                            renderPosts();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            showAlert("Error", "Failed to post comment: " + ex.getMessage());
                            submitBtn.setDisable(false);
                        });
                    }
                }).start();
            }
        });

        content.getChildren().addAll(scrollPane, commentInput, submitBtn);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private VBox buildCommentView(CommentDTO comment) {
        Image avatarImage = defaultAvatar;
        if (comment.getUserAvatarUrl() != null && !comment.getUserAvatarUrl().isEmpty()) {
            try {
                avatarImage = new Image(comment.getUserAvatarUrl(), true);
            } catch (Exception e) {
                avatarImage = defaultAvatar;
            }
        }
        
        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(32);
        avatar.setFitHeight(32);
        avatar.setClip(new Circle(16, 16, 16));

        Label username = new Label("@" + comment.getUsername());
        username.setStyle("-fx-font-weight: bold;");
        
        Label timeLabel = new Label(formatTimeAgo(comment.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);

        VBox textContent = new VBox(2, username, contentLabel, timeLabel);
        HBox commentBox = new HBox(8, avatar, textContent);
        commentBox.setAlignment(Pos.TOP_LEFT);
        commentBox.setPadding(new Insets(8));
        commentBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");

        VBox commentContainer = new VBox(commentBox);
        
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            VBox repliesBox = new VBox(4);
            repliesBox.setPadding(new Insets(0, 0, 0, 40));
            for (CommentDTO reply : comment.getReplies()) {
                repliesBox.getChildren().add(buildCommentView(reply));
            }
            commentContainer.getChildren().add(repliesBox);
        }

        return commentContainer;
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

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Just now";
        
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "Just now";
        if (seconds < 3600) return (seconds / 60) + "m ago";
        if (seconds < 86400) return (seconds / 3600) + "h ago";
        if (seconds < 604800) return (seconds / 86400) + "d ago";
        
        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}