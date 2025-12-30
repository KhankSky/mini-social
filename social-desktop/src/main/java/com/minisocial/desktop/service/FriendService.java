package com.minisocial.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minisocial.desktop.config.AppConfig;
import com.minisocial.desktop.dto.FriendDTO;
import com.minisocial.desktop.dto.FriendRequestDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class FriendService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FriendService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AppConfig.TIMEOUT))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<FriendDTO> getFriends() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.FRIENDS_ENDPOINT))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, FriendDTO.class));
        } else {
            throw new IOException("Failed to fetch friends: " + response.statusCode());
        }
    }

    public List<FriendRequestDTO> getPendingRequests() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.FRIENDS_ENDPOINT + "/requests/pending"))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, FriendRequestDTO.class));
        } else {
            throw new IOException("Failed to fetch pending requests: " + response.statusCode());
        }
    }

    public void acceptRequest(Long requestId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/requests/%d/accept", AppConfig.FRIENDS_ENDPOINT, requestId)))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to accept request: " + response.statusCode());
        }
    }

    public void rejectRequest(Long requestId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/requests/%d/reject", AppConfig.FRIENDS_ENDPOINT, requestId)))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to reject request: " + response.statusCode());
        }
    }

    public void sendRequest(Long receiverId) throws IOException, InterruptedException {
        String json = String.format("{\"receiverId\": %d}", receiverId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.FRIENDS_ENDPOINT + "/requests"))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IOException("Failed to send request: " + response.statusCode());
        }
    }



    public void unfriend(Long friendId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%d", AppConfig.FRIENDS_ENDPOINT, friendId)))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new IOException("Failed to unfriend: " + response.statusCode());
        }
    }
}
