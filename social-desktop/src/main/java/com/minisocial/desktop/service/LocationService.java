package com.minisocial.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minisocial.desktop.config.AppConfig;
import com.minisocial.desktop.dto.LocationDTO;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class LocationService {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public LocationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AppConfig.TIMEOUT))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Search locations by query string
     * @param query Search query
     * @param limit Maximum number of results
     * @return List of locations
     */
    public List<LocationDTO> searchLocations(String query, Integer limit) throws IOException, InterruptedException {
        if (query == null || query.trim().isEmpty()) {
            return Arrays.asList();
        }
        
        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = String.format("%s/locations/search?q=%s&limit=%d", 
                AppConfig.BASE_URL, encodedQuery, limit != null ? limit : 5);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            LocationDTO[] locations = objectMapper.readValue(response.body(), LocationDTO[].class);
            return Arrays.asList(locations);
        } else {
            throw new IOException("Failed to search locations: " + response.statusCode());
        }
    }
    
    /**
     * Reverse geocode - get location from coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @return Location information
     */
    public LocationDTO reverseGeocode(Double lat, Double lon) throws IOException, InterruptedException {
        String url = String.format("%s/locations/reverse?lat=%f&lon=%f", 
                AppConfig.BASE_URL, lat, lon);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AppConfig.AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), LocationDTO.class);
        } else if (response.statusCode() == 404) {
            return null;
        } else {
            throw new IOException("Failed to reverse geocode: " + response.statusCode());
        }
    }
} 