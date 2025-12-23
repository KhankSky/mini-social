package com.minisocial.desktop;

public class UserSession {
    private Long userId = 1L; // Default ID for mockup
    private String username = "guest";
    private String fullName = "Guest User";
    private String role = "USER"; // Default role

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void clear() {
        this.userId = null;
        this.username = null;
        this.fullName = null;
        this.role = null;
    }
}