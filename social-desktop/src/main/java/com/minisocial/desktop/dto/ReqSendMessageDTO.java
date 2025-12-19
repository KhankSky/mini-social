package com.minisocial.desktop.dto;

import java.io.File;
import java.util.List;

public class ReqSendMessageDTO {
    private Long receiverId;
    private String content;
    private List<File> files;

    public ReqSendMessageDTO() {
    }

    public ReqSendMessageDTO(Long receiverId, String content) {
        this.receiverId = receiverId;
        this.content = content;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}
