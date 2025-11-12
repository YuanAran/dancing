package com.dancing.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 */
@Data
public class Post {
    private Integer id;
    private String title;           // 帖子标题
    private String content;         // 帖子内容
    private Integer userId;         // 发布者ID
    private String username;        // 发布者用户名（从user表关联）
    private Integer likesCount;     // 点赞数
    private Boolean isLiked;        // 当前用户是否已点赞
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post() {
    }

    public Post(String title, String content, Integer userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.likesCount = 0;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", likesCount=" + likesCount +
                ", isLiked=" + isLiked +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
