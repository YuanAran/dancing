-- 好友关系表
CREATE TABLE friendships (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    friend_id INT NOT NULL COMMENT '好友ID',
    status ENUM('pending', 'accepted', 'blocked') DEFAULT 'pending' COMMENT '好友状态：pending-待确认，accepted-已接受，blocked-已屏蔽',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_friendship (user_id, friend_id)
) COMMENT='好友关系表';
