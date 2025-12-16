package com.minisocial.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minisocial.desktop.config.AppConfig;
import com.minisocial.desktop.dto.CommentDTO;
import com.minisocial.desktop.dto.CreateCommentRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class CommentService {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public CommentService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AppConfig.TIMEOUT))
                .build();
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Get all comments for a post
     * @param postId Post ID
     * @return List of CommentDTO
     */
    public List<CommentDTO> getCommentsByPost(Long postId) throws IOException, InterruptedException {
        String url = String.format(AppConfig.COMMENTS_ENDPOINT, postId);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<CommentDTO>>() {});
        } else {
            throw new IOException("Failed to fetch comments: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Create a new comment
     * @param postId Post ID
     * @param request CreateCommentRequest containing comment data
     * @return Created CommentDTO
     */
    public CommentDTO createComment(Long postId, CreateCommentRequest request) throws IOException, InterruptedException {
        String url = String.format(AppConfig.COMMENTS_ENDPOINT, postId);
        String jsonBody = objectMapper.writeValueAsString(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), CommentDTO.class);
        } else {
            throw new IOException("Failed to create comment: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Create a reply to a comment
     * @param postId Post ID
     * @param parentCommentId Parent comment ID
     * @param content Reply content
     * @return Created CommentDTO
     */
    public CommentDTO createReply(Long postId, Long parentCommentId, String content) throws IOException, InterruptedException {
        CreateCommentRequest request = new CreateCommentRequest(content, parentCommentId);
        return createComment(postId, request);
    }
}