package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户动态实体
 */
@Entity
@Table(name = "user_activities")
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "content", length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_video_id")
    private Video targetVideo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Column(name = "metadata", length = 1000)
    private String metadata; // JSON格式的额外数据

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    // 活动类型枚举
    public enum ActivityType {
        UPLOAD_VIDEO("上传了视频"),
        LIKE_VIDEO("点赞了视频"),
        COMMENT_VIDEO("评论了视频"),
        FOLLOW_USER("关注了用户"),
        SHARE_VIDEO("分享了视频"),
        FAVORITE_VIDEO("收藏了视频"),
        ACHIEVEMENT("获得了成就"),
        LEVEL_UP("等级提升");

        private final String description;

        ActivityType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造函数
    public UserActivity() {
        this.createdAt = LocalDateTime.now();
    }

    public UserActivity(User user, ActivityType activityType) {
        this();
        this.user = user;
        this.activityType = activityType;
    }

    public UserActivity(User user, ActivityType activityType, String content) {
        this(user, activityType);
        this.content = content;
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

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Video getTargetVideo() {
        return targetVideo;
    }

    public void setTargetVideo(Video targetVideo) {
        this.targetVideo = targetVideo;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * 生成活动描述
     */
    public String generateDescription() {
        StringBuilder description = new StringBuilder();
        description.append(activityType.getDescription());
        
        if (targetVideo != null) {
            description.append(" 《").append(targetVideo.getTitle()).append("》");
        }
        
        if (targetUser != null) {
            description.append(" ").append(targetUser.getNickname() != null ? 
                targetUser.getNickname() : targetUser.getUsername());
        }
        
        if (content != null && !content.trim().isEmpty()) {
            description.append(": ").append(content);
        }
        
        return description.toString();
    }
}
