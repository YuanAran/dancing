package com.dancing.controller;

import com.dancing.common.ApiResponse;
import com.dancing.common.JwtContext;
import com.dancing.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 视频通话控制器
 */
@Controller
public class VideoCallController {

    @Autowired
    private JwtContext jwtContext;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 存储房间信息：房间ID -> 房间信息
    private static final Map<String, RoomInfo> rooms = new ConcurrentHashMap<>();

    /**
     * 创建或加入房间
     */
    @PostMapping("/api/video-call/create-room")
    @ResponseBody
    public ApiResponse<Map<String, Object>> createRoom(@RequestBody Map<String, Object> params,
                                                       HttpServletRequest request) {
        try {
            User currentUser = jwtContext.getCurrentUser(request);
            if (currentUser == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            String roomId = (String) params.get("roomId");
            Integer targetUserId = (Integer) params.get("targetUserId");

            if (roomId == null || roomId.isEmpty()) {
                // 生成房间ID
                roomId = "room_" + System.currentTimeMillis() + "_" + currentUser.getId();
            }

            RoomInfo room = rooms.get(roomId);
            if (room == null) {
                room = new RoomInfo();
                room.setRoomId(roomId);
                room.setCreatorId(currentUser.getId());
                room.setCreatorName(currentUser.getUsername());
                if (targetUserId != null) {
                    room.setTargetUserId(targetUserId);
                }
                rooms.put(roomId, room);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("roomId", roomId);
            result.put("userId", currentUser.getId());
            result.put("username", currentUser.getUsername());
            result.put("creatorId", room.getCreatorId());
            result.put("creatorName", room.getCreatorName());
            result.put("isCreator", room.getCreatorId() != null && room.getCreatorId().equals(currentUser.getId()));

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("创建房间失败：" + e.getMessage());
        }
    }

    /**
     * 获取房间信息
     */
    @GetMapping("/api/video-call/room/{roomId}")
    @ResponseBody
    public ApiResponse<RoomInfo> getRoomInfo(@PathVariable String roomId,
                                            HttpServletRequest request) {
        try {
            User currentUser = jwtContext.getCurrentUser(request);
            if (currentUser == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            RoomInfo room = rooms.get(roomId);
            if (room == null) {
                return ApiResponse.notFound("房间不存在");
            }

            return ApiResponse.success(room);
        } catch (Exception e) {
            return ApiResponse.error("获取房间信息失败：" + e.getMessage());
        }
    }

    /**
     * 离开房间
     */
    @PostMapping("/api/video-call/leave-room")
    @ResponseBody
    public ApiResponse<String> leaveRoom(@RequestBody Map<String, String> params,
                                        HttpServletRequest request) {
        try {
            User currentUser = jwtContext.getCurrentUser(request);
            if (currentUser == null) {
                return ApiResponse.unauthorized("用户未登录");
            }

            String roomId = params.get("roomId");
            if (roomId != null) {
                RoomInfo room = rooms.get(roomId);
                if (room != null) {
                    // 通知房间内其他用户
                    messagingTemplate.convertAndSend("/topic/room/" + roomId, 
                        createMessage("user-left", currentUser.getId(), currentUser.getUsername()));
                    
                    // 如果房间为空，删除房间
                    rooms.remove(roomId);
                }
            }

            return ApiResponse.success("已离开房间");
        } catch (Exception e) {
            return ApiResponse.error("离开房间失败：" + e.getMessage());
        }
    }

    /**
     * WebSocket消息处理 - 处理信令消息
     * 接收来自/app/video-call/signal的消息，转发到/topic/room/{roomId}
     */
    @MessageMapping("/video-call/signal")
    public void handleSignal(@Payload Map<String, Object> message) {
        String roomId = (String) message.get("roomId");
        if (roomId != null) {
            // 转发信令消息到房间内所有用户
            messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
        }
    }

    /**
     * 创建消息对象
     */
    private Map<String, Object> createMessage(String type, Integer userId, String username) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("userId", userId);
        message.put("username", username);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * 房间信息类
     */
    public static class RoomInfo {
        private String roomId;
        private Integer creatorId;
        private String creatorName;
        private Integer targetUserId;
        private Long createdAt;

        public RoomInfo() {
            this.createdAt = System.currentTimeMillis();
        }

        // Getters and Setters
        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public Integer getCreatorId() {
            return creatorId;
        }

        public void setCreatorId(Integer creatorId) {
            this.creatorId = creatorId;
        }

        public String getCreatorName() {
            return creatorName;
        }

        public void setCreatorName(String creatorName) {
            this.creatorName = creatorName;
        }

        public Integer getTargetUserId() {
            return targetUserId;
        }

        public void setTargetUserId(Integer targetUserId) {
            this.targetUserId = targetUserId;
        }

        public Long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Long createdAt) {
            this.createdAt = createdAt;
        }
    }
}

