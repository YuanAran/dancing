package com.dancing.controller;

import com.dancing.common.ApiResponse;
import com.dancing.common.JwtContext;
import com.dancing.dto.LiveRoomEntry;
import com.dancing.entity.User;
import com.dancing.service.LiveRoomRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 直播列表：上报（主播创建时）与查询（任意设备拉取）
 */
@RestController
@RequestMapping("/api/live")
public class LiveRoomController {

    @Autowired
    private JwtContext jwtContext;

    @Autowired
    private LiveRoomRegistry liveRoomRegistry;

    /**
     * 主播创建直播间后上报，局域网内其它设备可拉取到该条目
     */
    @PostMapping("/announce")
    public ApiResponse<Void> announce(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            User current = jwtContext.getCurrentUser(request);
            if (current == null) {
                return ApiResponse.unauthorized("用户未登录");
            }
            String roomId = body.get("roomId");
            String title = body.get("title");
            liveRoomRegistry.announce(roomId, title, current.getId(), current.getUsername());
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("上报失败：" + e.getMessage());
        }
    }

    /**
     * 公开：无需登录，便于手机等设备仅浏览列表（进入观看仍要登录）
     */
    @GetMapping("/rooms")
    public ApiResponse<List<LiveRoomEntry>> listRooms() {
        try {
            return ApiResponse.success(liveRoomRegistry.listOrderByNewest());
        } catch (Exception e) {
            return ApiResponse.error("获取列表失败：" + e.getMessage());
        }
    }
}
