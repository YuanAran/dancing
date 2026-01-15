package com.dancing.mapper;

import com.dancing.entity.Friendship;
import com.dancing.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 好友关系数据访问接口
 */
@Mapper
public interface FriendshipMapper {

    /**
     * 添加好友申请
     */
    @Insert("INSERT INTO friendships(user_id, friend_id, status) VALUES(#{userId}, #{friendId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Friendship friendship);

    /**
     * 更新好友状态
     */
    @Update("UPDATE friendships SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 根据用户ID和好友ID查找好友关系
     */
    @Select("SELECT * FROM friendships WHERE user_id = #{userId} AND friend_id = #{friendId}")
    Friendship findByUserAndFriend(@Param("userId") Integer userId, @Param("friendId") Integer friendId);

    /**
     * 查找用户的所有好友（已接受状态，双向关系）
     */
    @Select("SELECT DISTINCT u.*, MAX(f.updated_at) as last_updated FROM users u " +
            "INNER JOIN friendships f ON (u.id = f.friend_id OR u.id = f.user_id) " +
            "WHERE ((f.user_id = #{userId} AND u.id = f.friend_id) " +
            "OR (f.friend_id = #{userId} AND u.id = f.user_id)) " +
            "AND f.status = 'accepted' " +
            "AND u.id != #{userId} " +
            "GROUP BY u.id " +
            "ORDER BY last_updated DESC")
    List<User> findFriendsByUserId(Integer userId);

    /**
     * 查找待确认的好友申请
     */
    @Select("SELECT u.* FROM users u " +
            "INNER JOIN friendships f ON u.id = f.user_id " +
            "WHERE f.friend_id = #{userId} AND f.status = 'pending' " +
            "ORDER BY f.created_at DESC")
    List<User> findPendingRequests(Integer userId);

    /**
     * 查找用户发送的好友申请
     */
    @Select("SELECT u.* FROM users u " +
            "INNER JOIN friendships f ON u.id = f.friend_id " +
            "WHERE f.user_id = #{userId} AND f.status = 'pending' " +
            "ORDER BY f.created_at DESC")
    List<User> findSentRequests(Integer userId);

    /**
     * 删除好友关系
     */
    @Delete("DELETE FROM friendships WHERE (user_id = #{userId} AND friend_id = #{friendId}) OR (user_id = #{friendId} AND friend_id = #{userId})")
    int deleteFriendship(@Param("userId") Integer userId, @Param("friendId") Integer friendId);

    /**
     * 根据用户ID和好友ID更新好友状态
     */
    @Update("UPDATE friendships SET status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int updateStatusByUserAndFriend(@Param("userId") Integer userId, @Param("friendId") Integer friendId, @Param("status") String status);

    /**
     * 检查是否为好友
     */
    @Select("SELECT COUNT(*) FROM friendships " +
            "WHERE ((user_id = #{userId} AND friend_id = #{friendId}) " +
            "OR (user_id = #{friendId} AND friend_id = #{userId})) " +
            "AND status = 'accepted'")
    int checkFriendship(@Param("userId") Integer userId, @Param("friendId") Integer friendId);

    /**
     * 搜索用户（排除自己和已经是好友的用户）
     */
    @Select("SELECT u.* FROM users u " +
            "WHERE u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "AND u.id != #{userId} " +
            "AND u.id NOT IN (" +
            "SELECT CASE WHEN f.user_id = #{userId} THEN f.friend_id ELSE f.user_id END " +
            "FROM friendships f " +
            "WHERE (f.user_id = #{userId} OR f.friend_id = #{userId}) " +
            "AND f.status IN ('accepted', 'pending')" +
            ") " +
            "ORDER BY u.created_at DESC")
    List<User> searchUsers(@Param("keyword") String keyword, @Param("userId") Integer userId);
}
