package com.minisocial.desktop;

public class UserSession {
    private String username = "guest";
    private String fullName = "Guest User";

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
}