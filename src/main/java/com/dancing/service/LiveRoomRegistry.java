package com.dancing.service;

import com.dancing.dto.LiveRoomEntry;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 全服可见的直播列表（内存）。用于局域网内多设备从同一后端拉取直播间，而非各浏览器 localStorage 各自为政。
 */
@Component
public class LiveRoomRegistry {

    private final ConcurrentHashMap<String, LiveRoomEntry> rooms = new ConcurrentHashMap<>();

    public void announce(String roomId, String title, Integer creatorId, String creatorName) {
        if (roomId == null || !roomId.startsWith("live_")) {
            throw new IllegalArgumentException("直播间 ID 必须以 live_ 开头");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        String t = title.trim();
        if (t.length() > 120) {
            t = t.substring(0, 120);
        }
        long now = System.currentTimeMillis();
        rooms.put(roomId, new LiveRoomEntry(roomId, t, creatorId, creatorName, now));
    }

    public List<LiveRoomEntry> listOrderByNewest() {
        return rooms.values().stream()
                .sorted(Comparator.comparingLong(LiveRoomEntry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public void remove(String roomId) {
        if (roomId != null && roomId.startsWith("live_")) {
            rooms.remove(roomId);
        }
    }
}
