package com.dancing.mapper;

import com.dancing.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 评论数据访问接口
 */
@Mapper
public interface CommentMapper {

    /**
     * 插入新评论
     */
    @Insert("INSERT INTO comments(content, user_id, video_id, post_id) VALUES(#{content}, #{userId}, #{videoId}, #{postId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    /**
     * 根据视频ID查找评论
     */
    @Select("SELECT * FROM comments WHERE video_id = #{videoId} ORDER BY created_at DESC")
    List<Comment> findByVideoId(Integer videoId);

    /**
     * 根据帖子ID查找评论
     */
    @Select("SELECT c.id, c.content, c.user_id, c.video_id, c.post_id, c.created_at, u.username " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} " +
            "ORDER BY c.created_at DESC")
    List<Comment> findByPostId(Integer postId);

    @Select("SELECT c.id, c.content, c.user_id, c.video_id, c.post_id, c.created_at, u.username " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.id = #{id}")
    Comment findById(Integer id);

    /**
     * 删除评论
     */
    @Delete("DELETE FROM comments WHERE id = #{id}")
    int deleteById(Integer id);
}
