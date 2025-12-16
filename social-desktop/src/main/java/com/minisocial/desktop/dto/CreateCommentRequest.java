package com.minisocial.desktop.dto;

public class CreateCommentRequest {
    
    private String content;
    private Long parentCommentId;
    
    // Constructors
    public CreateCommentRequest() {}
    
    public CreateCommentRequest(String content) {
        this.content = content;
    }
    
    public CreateCommentRequest(String content, Long parentCommentId) {
        this.content = content;
        this.parentCommentId = parentCommentId;
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
}