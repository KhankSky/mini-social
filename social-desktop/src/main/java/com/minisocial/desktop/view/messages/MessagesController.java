package com.minisocial.desktop.view.messages;

import com.minisocial.desktop.AppNavigator;
import com.minisocial.desktop.UserSession;
import com.minisocial.desktop.dto.ConversationDTO;
import com.minisocial.desktop.dto.MessageDTO;
import com.minisocial.desktop.dto.ReqSendMessageDTO;
import com.minisocial.desktop.service.MessageService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import com.minisocial.desktop.dto.FriendDTO;
import com.minisocial.desktop.service.FriendService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MessagesController {
    private final UserSession session;
    private final MessageService messageService;
    private final com.minisocial.desktop.service.WebSocketService webSocketService;
    private final Long targetUserId;

    @FXML
    private VBox conversationList;
    @FXML
    private VBox emptyChatView;
    @FXML
    private VBox activeChatView;
    @FXML
    private VBox messagesContainer;
    @FXML
    private Label chatPartnerName;
    @FXML
    private Label chatPartnerStatus;
    @FXML
    private Circle chatPartnerAvatar;
    @FXML
    private TextArea messageInput;
    @FXML
    private ScrollPane messageScrollPane;

    @FXML
    private FlowPane filePreviewArea;
    @FXML
    private TextField searchField;

    private ConversationDTO activeConversation;
    private final java.util.List<java.io.File> selectedFiles = new java.util.ArrayList<>();
    private List<ConversationDTO> allConversations = new java.util.ArrayList<>();
    private List<FriendDTO> allFriends = new java.util.ArrayList<>();

    public MessagesController(AppNavigator navigator, UserSession session, Long targetUserId) {
        this.session = session;
        this.messageService = new MessageService();
        this.webSocketService = new com.minisocial.desktop.service.WebSocketService();
        this.targetUserId = targetUserId;
    }

    public MessagesController(AppNavigator navigator, UserSession session) {
        this(navigator, session, null);
    }

    @FXML
    private void initialize() {
        loadConversations();
        connectWebSocket();
    }

    private void connectWebSocket() {
        webSocketService.connect(this::handleNewMessage);
    }

    private void handleNewMessage(MessageDTO newMessage) {
        if (activeConversation != null) {
            boolean isRelated = newMessage.getSenderId().equals(activeConversation.getUserId())
                    || newMessage.getReceiverId().equals(activeConversation.getUserId());

            if (isRelated) {
                Platform.runLater(() -> {
                    // Avoid duplicate messages if already added via REST response
                    boolean alreadyExists = messagesContainer.getChildren().stream()
                            .filter(node -> node instanceof HBox)
                            .anyMatch(node -> {
                                // This is a bit hacky but we can check the time and content if ID is
                                // available
                                // Better: Check if a message with the same ID is already there
                                return false; // Default for now, handleSendMessage will be smarter
                            });

                    // Check if it's already in the container by ID
                    // (Assuming we store the message ID in the bubble's properties)
                    boolean exists = false;
                    for (javafx.scene.Node node : messagesContainer.getChildren()) {
                        if (node.getUserData() != null && node.getUserData().equals(newMessage.getId())) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        javafx.scene.Node bubble = createMessageBubble(newMessage);
                        bubble.setUserData(newMessage.getId());
                        messagesContainer.getChildren().add(bubble);
                        messageScrollPane.setVvalue(1.0);
                    }
                });
            }
        }
        // Refresh conversation list to show badge/last message
        loadConversations();
    }

    public void stop() {
        webSocketService.disconnect();
    }

    private void loadConversations() {
        new Thread(() -> {
            try {
                List<ConversationDTO> conversations = messageService.getConversations();
                FriendService friendService = new FriendService();
                List<FriendDTO> friends = friendService.getFriends();

                Platform.runLater(() -> {
                    this.allConversations = conversations;
                    this.allFriends = friends;
                    filterConversations(searchField.getText());

                    ConversationDTO targetConv = null;
                    if (targetUserId != null) {
                        for (ConversationDTO conv : conversations) {
                            if (conv.getUserId().equals(targetUserId)) {
                                targetConv = conv;
                                break;
                            }
                        }
                    }

                    if (targetConv != null) {
                        selectConversation(targetConv);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleNewChat() {
        new Thread(() -> {
            try {
                FriendService friendService = new FriendService();
                List<FriendDTO> friends = friendService.getFriends();
                Platform.runLater(() -> showNewChatDialog(friends));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Failed to load friends: " + e.getMessage()));
            }
        }).start();
    }

    private void showNewChatDialog(List<FriendDTO> friends) {
        if (friends.isEmpty()) {
            showAlert("New Chat", "You don't have any friends yet to start a chat with.");
            return;
        }

        ChoiceDialog<FriendDTO> dialog = new ChoiceDialog<>(friends.get(0), friends);
        dialog.setTitle("New Chat");
        dialog.setHeaderText("Select a friend to start a conversation");
        dialog.setContentText("Friend:");

        // Custom string converter for the ChoiceBox
        dialog.getDialogPane().setPrefWidth(400);

        Optional<FriendDTO> result = dialog.showAndWait();
        result.ifPresent(friend -> {
            // Find if conversation already exists
            ConversationDTO existing = allConversations.stream()
                    .filter(c -> c.getUserId().equals(friend.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                selectConversation(existing);
            } else {
                // Create a temporary conversation entry
                ConversationDTO newConv = new ConversationDTO();
                newConv.setUserId(friend.getId());
                newConv.setUsername(friend.getUsername());
                newConv.setLastMessage("Start a new conversation");
                // Add to list and select
                allConversations.add(0, newConv);
                filterConversations(searchField.getText());
                selectConversation(newConv);
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        filterConversations(query);
    }

    private void filterConversations(String query) {
        conversationList.getChildren().clear();
        String lowerQuery = query == null ? "" : query.toLowerCase();

        // 1. Show matching existing conversations
        for (ConversationDTO conv : allConversations) {
            boolean matches = conv.getUsername().toLowerCase().contains(lowerQuery) ||
                    (conv.getLastMessage() != null && conv.getLastMessage().toLowerCase().contains(lowerQuery));

            if (matches) {
                conversationList.getChildren().add(createConversationItem(conv));
            }
        }

        // 2. Show matching friends who don't have a conversation yet
        if (!lowerQuery.isEmpty()) {
            for (FriendDTO friend : allFriends) {
                boolean alreadyInResults = allConversations.stream()
                        .anyMatch(c -> c.getUserId().equals(friend.getId()) &&
                                (c.getUsername().toLowerCase().contains(lowerQuery) ||
                                        (c.getLastMessage() != null
                                                && c.getLastMessage().toLowerCase().contains(lowerQuery))));

                if (!alreadyInResults && friend.getUsername().toLowerCase().contains(lowerQuery)) {
                    // Create a "virtual" conversation item for the friend
                    ConversationDTO virtualConv = new ConversationDTO();
                    virtualConv.setUserId(friend.getId());
                    virtualConv.setUsername(friend.getUsername());
                    virtualConv.setLastMessage("Start a new chat");

                    conversationList.getChildren().add(createConversationItem(virtualConv));
                }
            }
        }
    }

    @FXML
    private void handleVoiceCall() {
        showAlert("Voice Call", "Voice calling is currently only available on the Web version.");
    }

    @FXML
    private void handleVideoCall() {
        showAlert("Video Call", "Video calling is currently only available on the Web version.");
    }

    private HBox createConversationItem(ConversationDTO conv) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 24, 12, 24));
        item.setStyle("-fx-cursor: hand;");
        item.getStyleClass().add("conversation-item");

        Circle avatar = new Circle(22, Color.web("#6366f1"));

        VBox textInfo = new VBox(4);
        Label name = new Label(conv.getUsername());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        Label lastMsg = new Label(conv.getLastMessage() != null ? conv.getLastMessage() : "No messages yet");
        lastMsg.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        lastMsg.setMaxWidth(200);

        textInfo.getChildren().addAll(name, lastMsg);

        VBox meta = new VBox(4);
        meta.setAlignment(Pos.TOP_RIGHT);
        Label time = new Label(conv.getLastMessageTime() != null
                ? conv.getLastMessageTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "");
        time.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");

        if (conv.getUnreadCount() > 0) {
            Label badge = new Label(String.valueOf(conv.getUnreadCount()));
            badge.setStyle(
                    "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 10;");
            meta.getChildren().addAll(time, badge);
        } else {
            meta.getChildren().add(time);
        }

        item.getChildren().addAll(avatar, textInfo, meta);
        HBox.setHgrow(textInfo, javafx.scene.layout.Priority.ALWAYS);

        item.setOnMouseClicked(e -> selectConversation(conv));

        return item;
    }

    private void selectConversation(ConversationDTO conv) {
        this.activeConversation = conv;
        emptyChatView.setVisible(false);
        emptyChatView.setManaged(false);
        activeChatView.setVisible(true);
        activeChatView.setManaged(true);

        chatPartnerName.setText(conv.getUsername());
        loadMessages(conv.getUserId());
    }

    private void loadMessages(Long userId) {
        new Thread(() -> {
            try {
                List<MessageDTO> messages = messageService.getConversationMessages(userId);
                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();
                    for (MessageDTO msg : messages) {
                        javafx.scene.Node bubble = createMessageBubble(msg);
                        bubble.setUserData(msg.getId());
                        messagesContainer.getChildren().add(bubble);
                    }
                    messageScrollPane.setVvalue(1.0);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private HBox createMessageBubble(MessageDTO msg) {
        boolean isMine = msg.getSenderId().equals(session.getUserId());

        HBox container = new HBox();
        container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        container.setMaxWidth(Double.MAX_VALUE);

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(10, 14, 10, 14));

        if (isMine) {
            bubble.setStyle("-fx-background-color: #6366f1; -fx-background-radius: 18 18 2 18;");
        } else {
            bubble.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 18 18 18 2; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1);");
        }

        // Attachments
        if (msg.getAttachments() != null && !msg.getAttachments().isEmpty()) {
            for (String url : msg.getAttachments()) {
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(url);
                imageView.setFitWidth(200);
                imageView.setPreserveRatio(true);
                bubble.getChildren().add(imageView);
            }
        }

        if (msg.getContent() != null && !msg.getContent().isEmpty()) {
            Text text = new Text(msg.getContent());
            text.setFill(isMine ? Color.WHITE : Color.web("#1f2937"));
            text.wrappingWidthProperty().set(370);
            TextFlow textFlow = new TextFlow(text);
            bubble.getChildren().add(textFlow);
        }

        Label time = new Label(msg.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (isMine ? "#e0e7ff" : "#9ca3af") + ";");
        bubble.getChildren().add(time);
        bubble.setAlignment(isMine ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);

        container.getChildren().add(bubble);
        return container;
    }

    @FXML
    private void handleAttachFile() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        List<java.io.File> files = fileChooser.showOpenMultipleDialog(messageInput.getScene().getWindow());
        if (files != null) {
            selectedFiles.addAll(files);
            updateFilePreview();
        }
    }

    private void updateFilePreview() {
        filePreviewArea.getChildren().clear();
        for (java.io.File file : selectedFiles) {
            HBox chip = new HBox(4);
            chip.setAlignment(Pos.CENTER_LEFT);
            chip.setStyle("-fx-background-color: #e0e7ff; -fx-padding: 4 8; -fx-background-radius: 16;");

            Label name = new Label(file.getName());
            name.setStyle("-fx-text-fill: #4338ca; -fx-font-size: 12px;");

            Button close = new Button("x");
            close.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #4338ca; -fx-font-weight: bold; -fx-padding: 0;");
            close.setOnAction(e -> {
                selectedFiles.remove(file);
                updateFilePreview();
            });

            chip.getChildren().addAll(name, close);
            filePreviewArea.getChildren().add(chip);
        }
    }

    @FXML
    private void handleSendMessage() {
        String content = messageInput.getText().trim();
        if ((content.isEmpty() && selectedFiles.isEmpty()) || activeConversation == null)
            return;

        messageInput.setDisable(true);
        // Create a copy of files to allow clearing the list immediately
        List<java.io.File> filesToSend = new java.util.ArrayList<>(selectedFiles);

        new Thread(() -> {
            try {
                MessageDTO sentMsg;
                if (!filesToSend.isEmpty()) {
                    messageService.sendMessageWithFiles(activeConversation.getUserId(), content, filesToSend);
                    sentMsg = null; // WebSocket will deliver the message with full details (urls)
                } else {
                    ReqSendMessageDTO req = new ReqSendMessageDTO(activeConversation.getUserId(), content);
                    sentMsg = messageService.sendMessage(req);
                }

                Platform.runLater(() -> {
                    if (sentMsg != null) {
                        // Check if already added by WebSocket (unlikely but possible)
                        boolean exists = false;
                        for (javafx.scene.Node node : messagesContainer.getChildren()) {
                            if (node.getUserData() != null && node.getUserData().equals(sentMsg.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            javafx.scene.Node bubble = createMessageBubble(sentMsg);
                            bubble.setUserData(sentMsg.getId());
                            messagesContainer.getChildren().add(bubble);
                        }
                    }
                    messageInput.clear();
                    messageInput.setDisable(false);
                    selectedFiles.clear();
                    updateFilePreview();
                    messageScrollPane.setVvalue(1.0);
                    loadConversations();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    messageInput.setDisable(false);
                    showAlert("Error", "Failed to send message: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
