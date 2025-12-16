package com.minisocial.desktop.config;

public class AppConfig {
    public static final String BASE_URL = "http://localhost:9090/api";
    public static final String AUTH_TOKEN = "Bearer your_token_here"; // Thay bằng token thực tế
    public static final String POSTS_ENDPOINT = BASE_URL + "/posts";
    public static final String COMMENTS_ENDPOINT = BASE_URL + "/posts/%d/comments"; 
    public static final String LOCATIONS_ENDPOINT = BASE_URL + "/locations";
    public static final int TIMEOUT = 30000;
    public static final String SERVER_BASE_URL = "http://localhost:9090";
    
    private AppConfig() {
    }
    
    /**
     * Convert relative URL to full URL for uploaded files
     * @param relativeUrl Relative URL from API 
     * @return Full URL 
     */
    public static String getFullImageUrl(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) {
            return null;
        }
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl;
        }
        if (relativeUrl.startsWith("/")) {
            return SERVER_BASE_URL + relativeUrl;
        } else {
            return SERVER_BASE_URL + "/" + relativeUrl;
        }
    }
}