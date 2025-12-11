-- 用户表
CREATE TABLE `users` (
                         `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                         `username` VARCHAR(255) NOT NULL COMMENT '用户名',
                         `password` VARCHAR(255) NOT NULL COMMENT '密码',
                         `email` VARCHAR(255) NOT NULL COMMENT '邮箱',
                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_users_username` (`username`),
                         UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- 帖子表
CREATE TABLE `posts` (

                         `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                         `title` VARCHAR(255) NOT NULL COMMENT '帖子标题',
                         `content` TEXT NOT NULL COMMENT '帖子内容',
                         `user_id` INT NOT NULL COMMENT '发布者ID，对应 users.id（逻辑关联，不建外键）',
                         `username` VARCHAR(255) DEFAULT NULL COMMENT '发布者用户名（冗余字段）',
                         `likes_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
                         `is_liked` TINYINT(1) DEFAULT 0 COMMENT '当前用户是否已点赞（可按需使用）',
                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',

                         PRIMARY KEY (`id`),
                         KEY `idx_posts_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';


-- 视频表
CREATE TABLE `videos` (

                          `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                          `title` VARCHAR(255) NOT NULL COMMENT '视频标题',
                          `description` TEXT DEFAULT NULL COMMENT '视频描述',
                          `file_path` VARCHAR(512) NOT NULL COMMENT '视频文件路径',
                          `uploader_id` INT NOT NULL COMMENT '上传者ID，对应 users.id（逻辑关联）',
                          `uploader_name` VARCHAR(255) DEFAULT NULL COMMENT '上传者用户名（冗余字段）',
                          `thumbnail_path` VARCHAR(512) DEFAULT NULL COMMENT '封面图片路径',
                          `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                          PRIMARY KEY (`id`),
                          KEY `idx_videos_uploader_id` (`uploader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';


-- 评论表
CREATE TABLE `comments` (

                            `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `content` TEXT NOT NULL COMMENT '评论内容',
                            `user_id` INT NOT NULL COMMENT '评论用户ID，对应 users.id（逻辑关联）',
                            `video_id` INT DEFAULT NULL COMMENT '评论的视频ID，对应 videos.id（逻辑关联）',
                            `post_id` INT DEFAULT NULL COMMENT '评论的帖子ID，对应 posts.id（逻辑关联）',
                            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                            PRIMARY KEY (`id`),
                            KEY `idx_comments_user_id` (`user_id`),
                            KEY `idx_comments_video_id` (`video_id`),
                            KEY `idx_comments_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';


-- 好友关系表
CREATE TABLE `friendships` (

                               `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `user_id` INT NOT NULL COMMENT '用户ID，对应 users.id（逻辑关联）',
                               `friend_id` INT NOT NULL COMMENT '好友ID，对应 users.id（逻辑关联）',
                               `status` VARCHAR(32) NOT NULL COMMENT '关系状态：pending/accepted/blocked',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',

                               PRIMARY KEY (`id`),
                               KEY `idx_friendships_user_id` (`user_id`),
                               KEY `idx_friendships_friend_id` (`friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';


-- 帖子点赞表（去掉外键，保留唯一约束和索引）
CREATE TABLE IF NOT EXISTS `post_likes` (
                                           `id` INT PRIMARY KEY AUTO_INCREMENT,
                                           `post_id` INT NOT NULL COMMENT '帖子ID',
                                           `user_id` INT NOT NULL COMMENT '点赞用户ID',
                                           `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
                                           UNIQUE KEY `unique_user_post` (`user_id`, `post_id`),
                                           INDEX `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

