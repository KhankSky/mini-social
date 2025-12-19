package com.minisocial.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minisocial.desktop.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class AuthService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AppConfig.TIMEOUT))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String login(String username, String password) throws IOException, InterruptedException {
        String url = AppConfig.BASE_URL + "/auth/login";
        Map<String, String> body = Map.of(
                "username", username,
                "password", password);
        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body(); // The JWT token
        } else if (response.statusCode() == 401) {
            throw new IOException("Invalid username or password.");
        } else {
            String errorBody = response.body();
            String message = "Login failed (Status: " + response.statusCode() + ")";
            if (errorBody != null && !errorBody.isEmpty()) {
                message += ": " + errorBody;
            }
            throw new IOException(message);
        }
    }

    public Map<String, Object> getCurrentUserDetails(String token) throws IOException, InterruptedException {
        String url = AppConfig.BASE_URL + "/auth/me";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Map.class);
        } else {
            throw new IOException("Failed to get user details: " + response.statusCode());
        }
    }
}
