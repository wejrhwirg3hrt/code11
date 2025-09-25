package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户观看历史实体
 */
@Entity
@Table(name = "view_history")
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "watch_duration", nullable = false)
    private Long watchDuration = 0L; // 观看时长（秒）

    @Column(name = "completion_rate")
    private Double completionRate = 0.0; // 完成率（0-1）

    @Column(name = "watch_count", nullable = false)
    private Integer watchCount = 1; // 观看次数

    @Column(name = "last_position")
    private Long lastPosition = 0L; // 最后观看位置（秒）

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    // 构造函数
    public ViewHistory() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ViewHistory(User user, Video video) {
        this();
        this.user = user;
        this.video = video;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public Long getWatchDuration() {
        return watchDuration;
    }

    public void setWatchDuration(Long watchDuration) {
        this.watchDuration = watchDuration;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Integer getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(Integer watchCount) {
        this.watchCount = watchCount;
    }

    public Long getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Long lastPosition) {
        this.lastPosition = lastPosition;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加观看次数
     */
    public void incrementWatchCount() {
        this.watchCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新观看进度
     */
    public void updateProgress(Long duration, Long position) {
        this.watchDuration = Math.max(this.watchDuration, duration);
        this.lastPosition = position;
        if (duration > 0 && this.video.getDuration() != null) {
            try {
                // 将时长字符串转换为秒数
                Long videoDurationSeconds = parseDurationToSeconds(this.video.getDuration());
                if (videoDurationSeconds > 0) {
                    this.completionRate = Math.min(1.0, (double) duration / (double) videoDurationSeconds);
                }
            } catch (Exception e) {
                // 如果解析失败，设置默认完成率
                this.completionRate = 0.0;
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 将时长字符串转换为秒数
     * 格式：MM:SS 或 HH:MM:SS
     */
    private Long parseDurationToSeconds(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return 0L;
        }

        String[] parts = duration.split(":");
        if (parts.length == 2) {
            // MM:SS 格式
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return (long) (minutes * 60 + seconds);
        } else if (parts.length == 3) {
            // HH:MM:SS 格式
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            return (long) (hours * 3600 + minutes * 60 + seconds);
        }

        return 0L;
    }
}
