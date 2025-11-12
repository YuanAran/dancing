package com.dancing.service;

import com.dancing.entity.Friendship;
import com.dancing.entity.User;
import com.dancing.mapper.FriendshipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友业务逻辑层
 */
@Service
public class FriendshipService {

    @Autowired
    private FriendshipMapper friendshipMapper;

    /**
     * 发送好友申请
     */
    public boolean sendFriendRequest(Integer userId, Integer friendId) {
        try {
            // 检查是否已经是好友或已发送申请
            Friendship existing = friendshipMapper.findByUserAndFriend(userId, friendId);
            if (existing != null) {
                return false; // 已经存在关系
            }

            // 检查反向关系
            Friendship reverse = friendshipMapper.findByUserAndFriend(friendId, userId);
            if (reverse != null) {
                return false; // 已经存在关系
            }

            // 创建好友申请
            Friendship friendship = new Friendship(userId, friendId, "pending");
            int result = friendshipMapper.insert(friendship);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 接受好友申请（通过用户ID和好友ID）
     */
    public boolean acceptFriendRequestByUserAndFriend(Integer userId, Integer friendId) {
        try {
            int result = friendshipMapper.updateStatusByUserAndFriend(friendId, userId, "accepted");
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 拒绝好友申请（通过用户ID和好友ID）
     */
    public boolean rejectFriendRequestByUserAndFriend(Integer userId, Integer friendId) {
        try {
            int result = friendshipMapper.deleteFriendship(friendId, userId);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除好友
     */
    public boolean deleteFriend(Integer userId, Integer friendId) {
        try {
            int result = friendshipMapper.deleteFriendship(userId, friendId);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取用户的好友列表
     */
    public List<User> getFriends(Integer userId) {
        return friendshipMapper.findFriendsByUserId(userId);
    }

    /**
     * 获取待确认的好友申请
     */
    public List<User> getPendingRequests(Integer userId) {
        return friendshipMapper.findPendingRequests(userId);
    }

    /**
     * 获取用户发送的好友申请
     */
    public List<User> getSentRequests(Integer userId) {
        return friendshipMapper.findSentRequests(userId);
    }

    /**
     * 搜索用户
     */
    public List<User> searchUsers(String keyword, Integer userId) {
        return friendshipMapper.searchUsers(keyword, userId);
    }

    /**
     * 检查是否为好友
     */
    public boolean isFriend(Integer userId, Integer friendId) {
        int count = friendshipMapper.checkFriendship(userId, friendId);
        return count > 0;
    }

    /**
     * 获取好友申请数量
     */
    public int getPendingRequestCount(Integer userId) {
        List<User> pendingRequests = getPendingRequests(userId);
        return pendingRequests.size();
    }
}
