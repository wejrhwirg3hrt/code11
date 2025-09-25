package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role = "USER";

    private Boolean enabled = true;

    private Boolean banned = false;

    @Column(name = "ban_reason")
    private String banReason;

    @Column(name = "warning_count")
    private Integer warningCount = 0;

    @Column(name = "experience")
    private Long experience = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private Long version = 0L;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "delete_reason")
    private String deleteReason;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "nickname", length = 100)
    private String nickname;

    // 积分系统
    @Column(name = "points")
    private Integer points = 0;

    // 签到系统
    @Column(name = "last_checkin_date")
    private LocalDate lastCheckinDate;

    @Column(name = "consecutive_checkin_days")
    private Integer consecutiveCheckinDays = 0;

    @Column(name = "total_checkin_days")
    private Integer totalCheckinDays = 0;

    // 构造函数
    public User() {}

    // 添加PreUpdate注解方法
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getBanned() { return banned; }
    public void setBanned(Boolean banned) { this.banned = banned; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }

    public Integer getWarningCount() { return warningCount; }
    public void setWarningCount(Integer warningCount) { this.warningCount = warningCount; }

    public Long getExperience() { return experience; }
    public void setExperience(Long experience) { this.experience = experience; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    // 删除相关字段的getter和setter
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    // 便利方法，返回基本类型
    public boolean isDeleted() { return deleted != null && deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public boolean isEnabled() { return enabled != null && enabled; }
    public boolean isBanned() { return banned != null && banned; }
    public boolean isAdmin() { return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role); }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public String getDeleteReason() { return deleteReason; }
    public void setDeleteReason(String deleteReason) { this.deleteReason = deleteReason; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    // 积分系统 getter/setter
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    // 签到系统 getter/setter
    public LocalDate getLastCheckinDate() { return lastCheckinDate; }
    public void setLastCheckinDate(LocalDate lastCheckinDate) { this.lastCheckinDate = lastCheckinDate; }

    public Integer getConsecutiveCheckinDays() { return consecutiveCheckinDays; }
    public void setConsecutiveCheckinDays(Integer consecutiveCheckinDays) { this.consecutiveCheckinDays = consecutiveCheckinDays; }

    public Integer getTotalCheckinDays() { return totalCheckinDays; }
    public void setTotalCheckinDays(Integer totalCheckinDays) { this.totalCheckinDays = totalCheckinDays; }
}