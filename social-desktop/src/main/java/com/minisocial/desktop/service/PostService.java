package com.minisocial.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.minisocial.desktop.config.AppConfig;
import com.minisocial.desktop.dto.CreatePostRequest;
import com.minisocial.desktop.dto.PaginationDTO;
import com.minisocial.desktop.dto.PostDTO;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PostService {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public PostService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AppConfig.TIMEOUT))
                .build();
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Get all posts with pagination
     * @param page Page number (1-indexed)
     * @param size Page size
     * @return PaginationDTO containing posts
     */
    public PaginationDTO<PostDTO> getAllPosts(int page, int size) throws IOException, InterruptedException {
        String url = String.format("%s?page=%d&size=%d", AppConfig.POSTS_ENDPOINT, page, size);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), 
                    objectMapper.getTypeFactory().constructParametricType(PaginationDTO.class, PostDTO.class));
        } else {
            throw new IOException("Failed to fetch posts: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Get posts by user ID
     * @param userId User ID
     * @param page Page number (1-indexed)
     * @param size Page size
     * @return PaginationDTO containing user's posts
     */
    public PaginationDTO<PostDTO> getUserPosts(Long userId, int page, int size) throws IOException, InterruptedException {
        String url = String.format("%s/user/%d?page=%d&size=%d", AppConfig.POSTS_ENDPOINT, userId, page, size);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), 
                    objectMapper.getTypeFactory().constructParametricType(PaginationDTO.class, PostDTO.class));
        } else {
            throw new IOException("Failed to fetch user posts: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Create a new post with text and images
     * @param request CreatePostRequest containing post data
     * @param images List of image files to upload
     * @return Created PostDTO
     */
    public PostDTO createPost(CreatePostRequest request, List<File> images) throws IOException, InterruptedException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        
        // Build multipart body
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
        
        // Add content field
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"content\"\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
            writer.append(request.getContent()).append("\r\n");
            writer.flush();
        }
        
        // Add privacy field
        if (request.getPrivacy() != null && !request.getPrivacy().isEmpty()) {
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"privacy\"\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
            writer.append(request.getPrivacy()).append("\r\n");
            writer.flush();
        }
        
        // Add location field
        if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"location\"\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
            writer.append(request.getLocation()).append("\r\n");
            writer.flush();
        }
        
        // Add image files
        if (images != null && !images.isEmpty()) {
            for (File imageFile : images) {
                String mimeType = Files.probeContentType(imageFile.toPath());
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"images\"; filename=\"")
                      .append(imageFile.getName()).append("\"\r\n");
                writer.append("Content-Type: ").append(mimeType).append("\r\n");
                writer.append("Content-Transfer-Encoding: binary\r\n\r\n");
                writer.flush();
                
                // Write file bytes
                Files.copy(imageFile.toPath(), outputStream);
                outputStream.flush();
                
                writer.append("\r\n");
                writer.flush();
            }
        }
        
        // End of multipart
        writer.append("--").append(boundary).append("--\r\n");
        writer.close();
        
        byte[] multipartBody = outputStream.toByteArray();
        
        // Create HTTP request
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.POSTS_ENDPOINT))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), PostDTO.class);
        } else {
            throw new IOException("Failed to create post: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Create a post without images (convenience method)
     * @param request CreatePostRequest containing post data
     * @return Created PostDTO
     */
    public PostDTO createPost(CreatePostRequest request) throws IOException, InterruptedException {
        return createPost(request, new ArrayList<>());
    }
}