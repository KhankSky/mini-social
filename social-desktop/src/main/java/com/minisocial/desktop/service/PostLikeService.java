package com.minisocial.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minisocial.desktop.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PostLikeService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PostLikeService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AppConfig.TIMEOUT))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Like a post
     * @param postId Post ID to like
     */
    public void likePost(Long postId) throws IOException, InterruptedException {
        String url = String.format("%s/%d/like", AppConfig.POSTS_ENDPOINT, postId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to like post: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Unlike a post
     * @param postId Post ID to unlike
     */
    public void unlikePost(Long postId) throws IOException, InterruptedException {
        String url = String.format("%s/%d/like", AppConfig.POSTS_ENDPOINT, postId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to unlike post: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Check if post is liked by current user
     * @param postId Post ID to check
     * @return true if post is liked by current user, false otherwise
     */
    public boolean isPostLikedByCurrentUser(Long postId) throws IOException, InterruptedException {
        String url = String.format("%s/likes/post/%d/is-liked", AppConfig.BASE_URL, postId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return Boolean.parseBoolean(response.body());
        } else {
            throw new IOException("Failed to check if post is liked: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Get like count for a post
     * @param postId Post ID
     * @return number of likes
     */
    public Long getLikeCount(Long postId) throws IOException, InterruptedException {
        String url = String.format("%s/likes/post/%d/count", AppConfig.BASE_URL, postId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return Long.parseLong(response.body());
        } else {
            throw new IOException("Failed to get like count: " + response.statusCode() + " - " + response.body());
        }
    }
}