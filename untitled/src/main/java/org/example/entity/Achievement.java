package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 成就实体
 */
@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "icon", length = 255)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AchievementCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false)
    private AchievementRarity rarity;

    @Column(name = "points", nullable = false)
    private Integer points; // 成就奖励的经验值

    @Column(name = "condition_type", nullable = false, length = 50)
    private String conditionType; // 条件类型：VIDEO_COUNT, LIKE_COUNT, WATCH_TIME等

    @Column(name = "condition_value", nullable = false)
    private Long conditionValue; // 条件数值

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 成就分类枚举
    public enum AchievementCategory {
        BASIC("基础成就"),
        UPLOAD("上传相关"),
        SOCIAL("社交相关"),
        WATCH("观看相关"),
        MILESTONE("里程碑"),
        SPECIAL("特殊成就");

        private final String description;

        AchievementCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 成就稀有度枚举
    public enum AchievementRarity {
        COMMON("普通", "#6c757d"),
        UNCOMMON("不常见", "#28a745"),
        RARE("稀有", "#007bff"),
        EPIC("史诗", "#6f42c1"),
        LEGENDARY("传奇", "#fd7e14");

        private final String description;
        private final String color;

        AchievementRarity(String description, String color) {
            this.description = description;
            this.color = color;
        }

        public String getDescription() {
            return description;
        }

        public String getColor() {
            return color;
        }
    }

    // 构造函数
    public Achievement() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Achievement(String name, String description, AchievementCategory category, 
                      AchievementRarity rarity, Integer points, String conditionType, Long conditionValue) {
        this();
        this.name = name;
        this.description = description;
        this.category = category;
        this.rarity = rarity;
        this.points = points;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public AchievementCategory getCategory() {
        return category;
    }

    public void setCategory(AchievementCategory category) {
        this.category = category;
    }

    public AchievementRarity getRarity() {
        return rarity;
    }

    public void setRarity(AchievementRarity rarity) {
        this.rarity = rarity;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public Long getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(Long conditionValue) {
        this.conditionValue = conditionValue;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查用户是否满足成就条件
     */
    public boolean checkCondition(UserLevel userLevel) {
        switch (conditionType) {
            case "VIDEO_COUNT":
                return userLevel.getTotalVideosUploaded() >= conditionValue;
            case "LIKE_COUNT":
                return userLevel.getTotalLikesReceived() >= conditionValue;
            case "COMMENT_COUNT":
                return userLevel.getTotalCommentsMade() >= conditionValue;
            case "WATCH_TIME":
                return userLevel.getTotalWatchTime() >= conditionValue;
            case "LEVEL":
                return userLevel.getLevel() >= conditionValue;
            case "CONSECUTIVE_DAYS":
                return userLevel.getConsecutiveDays() >= conditionValue;
            default:
                return false;
        }
    }
}
