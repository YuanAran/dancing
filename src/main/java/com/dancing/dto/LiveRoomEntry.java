package com.dancing.dto;

/**
 * 局域网内可发现的直播条目（内存维护，服务重启后清空）
 */
public class LiveRoomEntry {
    private String roomId;
    private String title;
    private Integer creatorId;
    private String creatorName;
    private long createdAt;

    public LiveRoomEntry() {
    }

    public LiveRoomEntry(String roomId, String title, Integer creatorId, String creatorName, long createdAt) {
        this.roomId = roomId;
        this.title = title;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.createdAt = createdAt;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
