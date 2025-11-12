package com.dancing.entity;

import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
public class Friendship {
    private Integer id;
    private Integer userId;
    private Integer friendId;
    private String status; // pending, accepted, blocked
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Friendship() {}

    public Friendship(Integer userId, Integer friendId, String status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFriendId() {
        return friendId;
    }

    public void setFriendId(Integer friendId) {
        this.friendId = friendId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Friendship{" +
                "id=" + id +
                ", userId=" + userId +
                ", friendId=" + friendId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
