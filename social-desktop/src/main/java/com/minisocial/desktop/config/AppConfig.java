package com.minisocial.desktop.config;

public class AppConfig {
    public static final String BASE_URL = "http://152.42.186.105:8080/api";
    public static String AUTH_TOKEN = "Bearer your_token_here"; // Thay bằng token thực tế
    public static final String POSTS_ENDPOINT = BASE_URL + "/posts";
    public static final String COMMENTS_ENDPOINT = BASE_URL + "/posts/%d/comments";
    public static final String LOCATIONS_ENDPOINT = BASE_URL + "/locations";
    public static final String FRIENDS_ENDPOINT = BASE_URL + "/friends";
    public static final String MESSAGES_ENDPOINT = BASE_URL + "/messages";
    public static final int TIMEOUT = 30000;
    public static final String SERVER_BASE_URL = "http://152.42.186.105:8080";

    private AppConfig() {
    }

    /**
     * Convert relative URL to full URL for uploaded files
     * 
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