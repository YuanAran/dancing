package com.dancing.entity;

import java.time.LocalDateTime;

/**
 * 音乐实体类
 */
public class Music {
    private Integer id;
    private String title;
    private String description;
    private String filePath;
    private Integer uploaderId;
    private String uploaderName;
    private LocalDateTime createdAt;

    public Music() {
    }

    public Music(String title, String description, String filePath, Integer uploaderId) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.uploaderId = uploaderId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Integer uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
