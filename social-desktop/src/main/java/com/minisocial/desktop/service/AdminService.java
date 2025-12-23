package com.minisocial.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minisocial.desktop.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminService() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public Map<String, Object> getStatistics() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/admin/stats"))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<>() {
            });
        }
        throw new Exception("Failed to get statistics");
    }

    public List<Map<String, Object>> getAllUsers(int page, int size, String search) throws Exception {
        String url = AppConfig.SERVER_BASE_URL + "/api/admin/users?page=" + page + "&size=" + size;
        if (search != null && !search.isEmpty()) {
            url += "&search=" + search;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {
            });
            return (List<Map<String, Object>>) result.get("content");
        }
        throw new Exception("Failed to get users");
    }

    public void updateUserRole(Long userId, String role) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("role", role);
        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/admin/users/" + userId + "/role"))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to update user role");
        }
    }

    public void deleteUser(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/admin/users/" + userId))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to delete user");
        }
    }

    public List<Map<String, Object>> getAllPosts(int page, int size) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/admin/posts?page=" + page + "&size=" + size))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {
            });
            return (List<Map<String, Object>>) result.get("content");
        }
        throw new Exception("Failed to get posts");
    }

    public void deletePost(Long postId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.SERVER_BASE_URL + "/api/admin/posts/" + postId))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to delete post");
        }
    }
}
