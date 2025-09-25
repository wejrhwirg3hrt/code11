package org.example.dto;

import java.time.LocalDateTime;

/**
 * 实时评论消息DTO
 * 用于WebSocket实时通信
 */
public class RealtimeCommentMessage {
    private Long id;
    private String content;
    private Long videoId;
    private String username;
    private String userAvatar;
    private LocalDateTime createdAt;
    private String type; // COMMENT, DANMAKU, USER_JOIN, USER_LEAVE, LIKE, FAVORITE
    private Double time; // 弹幕时间点（秒）
    private String color; // 弹幕颜色
    private Integer fontSize; // 弹幕字体大小

    public RealtimeCommentMessage() {}

    public RealtimeCommentMessage(String content, Long videoId, String username) {
        this.content = content;
        this.videoId = videoId;
        this.username = username;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    @Override
    public String toString() {
        return "RealtimeCommentMessage{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", videoId=" + videoId +
                ", username='" + username + '\'' +
                ", type='" + type + '\'' +
                ", time=" + time +
                '}';
    }
}
