package com.dancing.entity;

import java.time.LocalDateTime;

/**
 * 视频实体类
 */
public class Video {
    private Integer id;
    private String title;
    private String description;
    private String filePath;
    private Integer uploaderId;
    private String uploaderName;  // 上传者用户名（从user表关联）
    private String thumbnailPath;
    private LocalDateTime createdAt;

    public Video() {}

    public Video(String title, String description, String filePath, Integer uploaderId) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.uploaderId = uploaderId;
    }

    // Getter和Setter方法
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

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", filePath='" + filePath + '\'' +
                ", uploaderId=" + uploaderId +
                ", uploaderName='" + uploaderName + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
