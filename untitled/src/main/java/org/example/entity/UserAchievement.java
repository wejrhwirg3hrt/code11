package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户成就关联实体
 */
@Entity
@Table(name = "user_achievements", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    @Column(name = "progress", nullable = false)
    private Double progress = 0.0; // 成就进度 0.0-1.0

    @Column(name = "is_displayed", nullable = false)
    private Boolean isDisplayed = true; // 是否在个人资料中显示

    @Column(name = "notification_sent", nullable = false)
    private Boolean notificationSent = false; // 是否已发送通知

    // 构造函数
    public UserAchievement() {
        this.unlockedAt = LocalDateTime.now();
    }

    public UserAchievement(User user, Achievement achievement) {
        this();
        this.user = user;
        this.achievement = achievement;
        this.progress = 1.0; // 解锁时进度为100%
    }

    public UserAchievement(User user, Achievement achievement, Double progress) {
        this();
        this.user = user;
        this.achievement = achievement;
        this.progress = progress;
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

    /**
     * 设置用户ID（便捷方法）
     */
    public void setUserId(Long userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }

    /**
     * 获取用户ID（便捷方法）
     */
    public Long getUserId() {
        return this.user != null ? this.user.getId() : null;
    }

    public Achievement getAchievement() {
        return achievement;
    }

    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Boolean getIsDisplayed() {
        return isDisplayed;
    }

    public void setIsDisplayed(Boolean isDisplayed) {
        this.isDisplayed = isDisplayed;
    }

    public Boolean getNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(Boolean notificationSent) {
        this.notificationSent = notificationSent;
    }

    /**
     * 更新进度
     */
    public void updateProgress(Double newProgress) {
        this.progress = Math.min(1.0, Math.max(0.0, newProgress));
    }

    /**
     * 检查是否已解锁
     */
    public boolean isUnlocked() {
        return progress >= 1.0;
    }

    /**
     * 获取进度百分比
     */
    public int getProgressPercentage() {
        return (int) (progress * 100);
    }
}
