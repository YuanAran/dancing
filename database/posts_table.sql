-- 先删除可能存在的外键约束和旧表
USE dancing;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;

-- 帖子表
CREATE TABLE posts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    user_id INT NOT NULL COMMENT '发布者ID',
    likes_count INT DEFAULT 0 COMMENT '点赞数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 帖子点赞表
CREATE TABLE post_likes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    post_id INT NOT NULL COMMENT '帖子ID',
    user_id INT NOT NULL COMMENT '点赞用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_post (user_id, post_id),
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

