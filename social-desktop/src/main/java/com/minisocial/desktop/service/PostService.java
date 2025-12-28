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
     * 
     * @param page 
     * @param size 
     * @return 
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
     * 
     * @param userId 
     * @param page 
     * @param size 
     * @return 
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
     * 
     * @param request 
     * @param images 
     * @return 
     */
    public PostDTO createPost(CreatePostRequest request, List<File> images) throws IOException, InterruptedException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Add text fields
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            writeTextField(outputStream, boundary, "content", request.getContent(), LINE_FEED);
        }
        
        if (request.getPrivacy() != null && !request.getPrivacy().isEmpty()) {
            writeTextField(outputStream, boundary, "privacy", request.getPrivacy(), LINE_FEED);
        }
        
        if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            writeTextField(outputStream, boundary, "location", request.getLocation(), LINE_FEED);
        }
        
        if (images != null && !images.isEmpty()) {
            for (File imageFile : images) {
                writeFileField(outputStream, boundary, "images", imageFile, LINE_FEED);
            }
        }
        
        outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
        
        byte[] multipartBody = outputStream.toByteArray();

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
    
 
    private void writeTextField(ByteArrayOutputStream outputStream, String boundary, 
                                String fieldName, String value, String lineFeed) throws IOException {
        outputStream.write(("--" + boundary + lineFeed).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"" + lineFeed).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: text/plain; charset=UTF-8" + lineFeed).getBytes(StandardCharsets.UTF_8));
        outputStream.write(lineFeed.getBytes(StandardCharsets.UTF_8));
        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
        outputStream.write(lineFeed.getBytes(StandardCharsets.UTF_8));
    }
    

    private void writeFileField(ByteArrayOutputStream outputStream, String boundary, 
                               String fieldName, File file, String lineFeed) throws IOException {
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        
        outputStream.write(("--" + boundary + lineFeed).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"" + lineFeed).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: " + mimeType + lineFeed).getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Transfer-Encoding: binary" + lineFeed).getBytes(StandardCharsets.UTF_8));
        outputStream.write(lineFeed.getBytes(StandardCharsets.UTF_8));
        
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        outputStream.write(lineFeed.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * @param request 
     * @return
     */
    public PostDTO createPost(CreatePostRequest request) throws IOException, InterruptedException {
        return createPost(request, new ArrayList<>());
    }
}