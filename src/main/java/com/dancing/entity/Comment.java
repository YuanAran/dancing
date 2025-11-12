package com.dancing.entity;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
public class Comment {
    private Integer id;
    private String content;
    private Integer userId;
    private Integer videoId;
    private Integer postId;
    private LocalDateTime createdAt;

    public Comment() {}

    public Comment(String content, Integer userId, Integer videoId, Integer postId) {
        this.content = content;
        this.userId = userId;
        this.videoId = videoId;
        this.postId = postId;
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getVideoId() {
        return videoId;
    }

    public void setVideoId(Integer videoId) {
        this.videoId = videoId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                ", videoId=" + videoId +
                ", postId=" + postId +
                ", createdAt=" + createdAt +
                '}';
    }
}
