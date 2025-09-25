package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户等级实体
 */
@Entity
@Table(name = "user_levels")
public class UserLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Column(name = "experience_points", nullable = false)
    private Long experiencePoints = 0L;

    @Column(name = "next_level_exp", nullable = false)
    private Long nextLevelExp = 100L;

    @Column(name = "total_videos_uploaded", nullable = false)
    private Integer totalVideosUploaded = 0;

    @Column(name = "total_likes_received", nullable = false)
    private Integer totalLikesReceived = 0;

    @Column(name = "total_comments_made", nullable = false)
    private Integer totalCommentsMade = 0;

    @Column(name = "total_watch_time", nullable = false)
    private Long totalWatchTime = 0L; // 总观看时长（秒）

    @Column(name = "consecutive_days", nullable = false)
    private Integer consecutiveDays = 0; // 连续登录天数

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_exp_gain", nullable = false)
    private LocalDateTime lastExpGain;

    // 构造函数
    public UserLevel() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastExpGain = LocalDateTime.now();
    }

    public UserLevel(User user) {
        this();
        this.user = user;
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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(Long experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public Long getNextLevelExp() {
        return nextLevelExp;
    }

    public void setNextLevelExp(Long nextLevelExp) {
        this.nextLevelExp = nextLevelExp;
    }

    public Integer getTotalVideosUploaded() {
        return totalVideosUploaded;
    }

    public void setTotalVideosUploaded(Integer totalVideosUploaded) {
        this.totalVideosUploaded = totalVideosUploaded;
    }

    public Integer getTotalLikesReceived() {
        return totalLikesReceived;
    }

    public void setTotalLikesReceived(Integer totalLikesReceived) {
        this.totalLikesReceived = totalLikesReceived;
    }

    public Integer getTotalCommentsMade() {
        return totalCommentsMade;
    }

    public void setTotalCommentsMade(Integer totalCommentsMade) {
        this.totalCommentsMade = totalCommentsMade;
    }

    public Long getTotalWatchTime() {
        return totalWatchTime;
    }

    public void setTotalWatchTime(Long totalWatchTime) {
        this.totalWatchTime = totalWatchTime;
    }

    public Integer getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(Integer consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
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

    public LocalDateTime getLastExpGain() {
        return lastExpGain;
    }

    public void setLastExpGain(LocalDateTime lastExpGain) {
        this.lastExpGain = lastExpGain;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加经验值
     */
    public boolean addExperience(Long exp) {
        this.experiencePoints += exp;
        this.lastExpGain = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        return checkLevelUp();
    }

    /**
     * 检查是否升级
     */
    public boolean checkLevelUp() {
        if (this.experiencePoints >= this.nextLevelExp) {
            this.level++;
            this.nextLevelExp = calculateNextLevelExp(this.level);
            return true;
        }
        return false;
    }

    /**
     * 计算下一级所需经验值
     */
    private Long calculateNextLevelExp(Integer level) {
        // 经验值计算公式：基础经验 * 等级^1.5
        return (long) (100 * Math.pow(level, 1.5));
    }

    /**
     * 获取当前等级进度百分比
     */
    public double getLevelProgress() {
        if (level == 1) {
            if (nextLevelExp == 0) return 0.0;
            return Math.min(100.0, Math.max(0.0, (double) experiencePoints / nextLevelExp * 100));
        }

        try {
            Long currentLevelExp = calculateNextLevelExp(level - 1);
            Long expInCurrentLevel = experiencePoints - currentLevelExp;
            Long expNeededForNextLevel = nextLevelExp - currentLevelExp;

            if (expNeededForNextLevel <= 0) return 100.0;
            if (expInCurrentLevel < 0) return 0.0;

            return Math.min(100.0, Math.max(0.0, (double) expInCurrentLevel / expNeededForNextLevel * 100));
        } catch (Exception e) {
            // 如果计算出错，返回基于当前经验值的简单百分比
            if (nextLevelExp == 0) return 0.0;
            return Math.min(100.0, Math.max(0.0, (double) experiencePoints / nextLevelExp * 100));
        }
    }

    /**
     * 获取等级称号
     */
    public String getLevelTitle() {
        if (level >= 50) return "传奇大师";
        if (level >= 40) return "超级达人";
        if (level >= 30) return "资深玩家";
        if (level >= 20) return "活跃用户";
        if (level >= 10) return "进阶用户";
        if (level >= 5) return "初级用户";
        return "新手用户";
    }

    /**
     * 增加上传视频数量
     */
    public void incrementVideosUploaded() {
        this.totalVideosUploaded++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加收到的点赞数
     */
    public void incrementLikesReceived() {
        this.totalLikesReceived++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加评论数
     */
    public void incrementCommentsMade() {
        this.totalCommentsMade++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加观看时长
     */
    public void addWatchTime(Long seconds) {
        this.totalWatchTime += seconds;
        this.updatedAt = LocalDateTime.now();
    }
}
