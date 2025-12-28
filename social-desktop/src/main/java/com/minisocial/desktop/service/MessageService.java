package com.minisocial.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minisocial.desktop.config.AppConfig;
import com.minisocial.desktop.dto.ConversationDTO;
import com.minisocial.desktop.dto.MessageDTO;
import com.minisocial.desktop.dto.ReqSendMessageDTO;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

public class MessageService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MessageService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AppConfig.TIMEOUT))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<ConversationDTO> getConversations() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.MESSAGES_ENDPOINT + "/conversations"))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ConversationDTO.class));
        } else {
            throw new IOException("Failed to fetch conversations: " + response.statusCode());
        }
    }

    public List<MessageDTO> getConversationMessages(Long userId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.MESSAGES_ENDPOINT + "/conversations/" + userId))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MessageDTO.class));
        } else {
            throw new IOException("Failed to fetch messages: " + response.statusCode());
        }
    }

    public MessageDTO sendMessage(ReqSendMessageDTO data) throws IOException, InterruptedException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

        // Add receiverId
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"receiverId\"\r\n\r\n");
        writer.append(String.valueOf(data.getReceiverId())).append("\r\n");

        // Add content
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"content\"\r\n\r\n");
        writer.append(data.getContent()).append("\r\n");
        writer.flush();

        // Add files
        if (data.getFiles() != null && !data.getFiles().isEmpty()) {
            for (File file : data.getFiles()) {
                String mimeType = Files.probeContentType(file.toPath());
                if (mimeType == null)
                    mimeType = "application/octet-stream";

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"files\"; filename=\"")
                        .append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: ").append(mimeType).append("\r\n\r\n");
                writer.flush();

                Files.copy(file.toPath(), outputStream);
                outputStream.flush();
                writer.append("\r\n");
                writer.flush();
            }
        }

        writer.append("--").append(boundary).append("--\r\n");
        writer.close();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.MESSAGES_ENDPOINT))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(outputStream.toByteArray()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), MessageDTO.class);
        } else {
            throw new IOException("Failed to send message: " + response.statusCode() + " - " + response.body());
        }
    }

    public void markAsRead(Long messageId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.MESSAGES_ENDPOINT + "/" + messageId + "/read"))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void markAllAsRead(Long userId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.MESSAGES_ENDPOINT + "/conversations/" + userId + "/read"))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
