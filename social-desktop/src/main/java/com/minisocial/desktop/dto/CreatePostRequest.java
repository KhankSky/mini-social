package com.minisocial.desktop.dto;

import java.io.File;
import java.util.List;


public class CreatePostRequest {
    
    private String content;
    private String privacy;
    private String location;   
    
    // Constructors
    public CreatePostRequest() {}
    
    public CreatePostRequest(String content, String privacy, String location) {
        this.content = content;
        this.privacy = privacy;
        this.location = location;
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getPrivacy() { return privacy; }
    public void setPrivacy(String privacy) { this.privacy = privacy; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

}