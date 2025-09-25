package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "admin_permissions")
public class AdminPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @ElementCollection(targetClass = PermissionType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "admin_permission_types",
                    joinColumns = @JoinColumn(name = "permission_id"))
    @Column(name = "permission_type")
    private Set<PermissionType> permissions;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private Boolean active = true;

    // 权限类型枚举
    public enum PermissionType {
        // 用户管理权限
        USER_VIEW("查看用户"),
        USER_EDIT("编辑用户"),
        USER_BAN("封禁用户"),
        USER_DELETE("删除用户"),

        // 视频管理权限
        VIDEO_VIEW("查看视频"),
        VIDEO_APPROVE("审核视频"),
        VIDEO_EDIT("编辑视频"),
        VIDEO_DELETE("删除视频"),
        VIDEO_BAN("封禁视频"),

        // 评论管理权限
        COMMENT_VIEW("查看评论"),
        COMMENT_EDIT("编辑评论"),
        COMMENT_DELETE("删除评论"),
        COMMENT_BAN("封禁评论"),

        // 内容管理权限
        CONTENT_STATISTICS("内容统计"),
        CONTENT_REPORT("内容举报处理"),
        CONTENT_CATEGORY("分类管理"),
        CONTENT_TAG("标签管理"),

        // 系统管理权限
        SYSTEM_CONFIG("系统配置"),
        SYSTEM_LOG("系统日志"),
        SYSTEM_MONITOR("系统监控"),
        SYSTEM_BACKUP("系统备份"),

        // 管理员管理权限（仅超级管理员）
        ADMIN_CREATE("创建管理员"),
        ADMIN_EDIT("编辑管理员"),
        ADMIN_BAN("封禁管理员"),
        ADMIN_PERMISSION("权限管理"),

        // CDN管理权限
        CDN_CONFIG("CDN配置"),
        CDN_CACHE("缓存管理"),
        CDN_STATISTICS("CDN统计"),

        // 公告管理权限
        ANNOUNCEMENT_CREATE("创建公告"),
        ANNOUNCEMENT_EDIT("编辑公告"),
        ANNOUNCEMENT_DELETE("删除公告");

        private final String description;

        PermissionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造函数和getter/setter
    public AdminPermission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public Set<PermissionType> getPermissions() { return permissions; }
    public void setPermissions(Set<PermissionType> permissions) { this.permissions = permissions; }

    public Long getGrantedBy() { return grantedBy; }
    public void setGrantedBy(Long grantedBy) { this.grantedBy = grantedBy; }

    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}