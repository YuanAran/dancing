package com.dancing.mapper;

import com.dancing.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 帖子Mapper接口
 */
@Mapper
public interface PostMapper {

    /**
     * 插入帖子
     */
    @Insert("INSERT INTO posts(title, content, user_id) VALUES(#{title}, #{content}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Post post);

    /**
     * 根据ID查询帖子（包含用户名）
     */
    @Select("SELECT p.id, p.title, p.content, p.user_id, p.likes_count, p.created_at, p.updated_at, " +
            "u.username " +
            "FROM posts p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "WHERE p.id = #{id}")
    Post findById(Integer id);

    /**
     * 查询所有帖子（包含用户名，按创建时间倒序）
     */
    @Select("SELECT p.id, p.title, p.content, p.user_id, p.likes_count, p.created_at, p.updated_at, " +
            "u.username " +
            "FROM posts p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "ORDER BY p.created_at DESC")
    List<Post> findAll();

    /**
     * 根据用户ID查询帖子
     */
    @Select("SELECT p.id, p.title, p.content, p.user_id, p.likes_count, p.created_at, p.updated_at, " +
            "u.username " +
            "FROM posts p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "WHERE p.user_id = #{userId} " +
            "ORDER BY p.created_at DESC")
    List<Post> findByUserId(Integer userId);

    /**
     * 根据标题搜索帖子
     */
    @Select("SELECT p.id, p.title, p.content, p.user_id, p.likes_count, p.created_at, p.updated_at, " +
            "u.username " +
            "FROM posts p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "WHERE p.title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.content LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY p.created_at DESC")
    List<Post> searchByKeyword(String keyword);

    /**
     * 更新帖子
     */
    @Update("UPDATE posts SET title = #{title}, content = #{content} WHERE id = #{id}")
    int update(Post post);

    /**
     * 删除帖子
     */
    @Delete("DELETE FROM posts WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 增加点赞数
     */
    @Update("UPDATE posts SET likes_count = likes_count + 1 WHERE id = #{postId}")
    int increaseLikesCount(Integer postId);

    /**
     * 减少点赞数
     */
    @Update("UPDATE posts SET likes_count = likes_count - 1 WHERE id = #{postId}")
    int decreaseLikesCount(Integer postId);

    /**
     * 添加点赞记录
     */
    @Insert("INSERT INTO post_likes(post_id, user_id) VALUES(#{postId}, #{userId})")
    int addLike(@Param("postId") Integer postId, @Param("userId") Integer userId);

    /**
     * 移除点赞记录
     */
    @Delete("DELETE FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    int removeLike(@Param("postId") Integer postId, @Param("userId") Integer userId);

    /**
     * 检查用户是否已点赞
     */
    @Select("SELECT COUNT(*) > 0 FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean isLikedByUser(@Param("postId") Integer postId, @Param("userId") Integer userId);

    /**
     * 获取帖子的点赞用户列表
     */
    @Select("SELECT u.* FROM users u " +
            "INNER JOIN post_likes pl ON u.id = pl.user_id " +
            "WHERE pl.post_id = #{postId} " +
            "ORDER BY pl.created_at DESC")
    List<com.dancing.entity.User> getLikeUsers(Integer postId);
}
