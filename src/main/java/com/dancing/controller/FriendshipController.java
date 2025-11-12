package com.dancing.controller;

import com.dancing.common.ApiResponse;
import com.dancing.common.JwtContext;
import com.dancing.entity.User;
import com.dancing.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友控制器 - 为Vue3前端提供好友相关RESTful接口
 */
@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private JwtContext jwtContext;

    /**
     * 搜索用户
     */
    @PostMapping("/search")
    public ApiResponse<List<User>> searchUsers(@RequestBody Map<String, String> params, 
                                               HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            String keyword = params.get("keyword");
            if (keyword == null || keyword.trim().isEmpty()) {
                return ApiResponse.badRequest("搜索关键词不能为空");
            }

            List<User> searchResults = friendshipService.searchUsers(keyword, user.getId());
            return ApiResponse.success(searchResults);
        } catch (Exception e) {
            return ApiResponse.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 发送好友申请
     */
    @PostMapping("/send-request")
    public ApiResponse<String> sendFriendRequest(@RequestBody Map<String, Object> params, 
                                                 HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            Integer friendId = (Integer) params.get("friendId");
            if (friendId == null) {
                return ApiResponse.badRequest("好友ID不能为空");
            }

            boolean success = friendshipService.sendFriendRequest(user.getId(), friendId);
            if (success) {
                return ApiResponse.success("好友申请已发送");
            } else {
                return ApiResponse.badRequest("发送失败，可能已经是好友或已发送申请");
            }
        } catch (Exception e) {
            return ApiResponse.error("发送失败：" + e.getMessage());
        }
    }

    /**
     * 接受好友申请
     */
    @PostMapping("/accept")
    public ApiResponse<String> acceptFriendRequest(@RequestBody Map<String, Object> params, 
                                                   HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            Integer friendId = (Integer) params.get("friendId");
            if (friendId == null) {
                return ApiResponse.badRequest("好友ID不能为空");
            }

            boolean success = friendshipService.acceptFriendRequestByUserAndFriend(user.getId(), friendId);
            if (success) {
                return ApiResponse.success("已接受好友申请");
            } else {
                return ApiResponse.error("操作失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 拒绝好友申请
     */
    @PostMapping("/reject")
    public ApiResponse<String> rejectFriendRequest(@RequestBody Map<String, Object> params, 
                                                   HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            Integer friendId = (Integer) params.get("friendId");
            if (friendId == null) {
                return ApiResponse.badRequest("好友ID不能为空");
            }

            boolean success = friendshipService.rejectFriendRequestByUserAndFriend(user.getId(), friendId);
            if (success) {
                return ApiResponse.success("已拒绝好友申请");
            } else {
                return ApiResponse.error("操作失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 删除好友
     */
    @PostMapping("/delete")
    public ApiResponse<String> deleteFriend(@RequestBody Map<String, Object> params, 
                                           HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            Integer friendId = (Integer) params.get("friendId");
            if (friendId == null) {
                return ApiResponse.badRequest("好友ID不能为空");
            }

            boolean success = friendshipService.deleteFriend(user.getId(), friendId);
            if (success) {
                return ApiResponse.success("已删除好友");
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取好友列表和待处理的好友申请
     */
    @GetMapping("/manage")
    public ApiResponse<Map<String, Object>> getFriendsAndRequests(HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            // 获取好友列表
            List<User> friends = friendshipService.getFriends(user.getId());

            // 获取待确认的好友申请
            List<User> pendingRequests = friendshipService.getPendingRequests(user.getId());

            // 获取发送的好友申请
            List<User> sentRequests = friendshipService.getSentRequests(user.getId());

            // 获取好友申请数量
            int pendingCount = friendshipService.getPendingRequestCount(user.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("friends", friends);
            result.put("pendingRequests", pendingRequests);
            result.put("sentRequests", sentRequests);
            result.put("pendingCount", pendingCount);

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取好友信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取待处理的好友申请列表
     */
    @GetMapping("/pending")
    public ApiResponse<List<User>> getPendingRequests(HttpServletRequest request) {
        try {
            User user = jwtContext.getCurrentUser(request);
            if (user == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            List<User> pendingRequests = friendshipService.getPendingRequests(user.getId());
            return ApiResponse.success(pendingRequests);
        } catch (Exception e) {
            return ApiResponse.error("获取好友申请失败：" + e.getMessage());
        }
    }
}

