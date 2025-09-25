package org.example.service;

import org.example.entity.AdminPermission;
import org.example.entity.AdminUser;
import org.example.repository.AdminPermissionRepository;
import org.example.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = false)
public class AdminPermissionService {

    @Autowired
    private AdminPermissionRepository permissionRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    public Set<AdminPermission.PermissionType> getAdminPermissions(Long adminId) {
        Optional<AdminPermission> permission = permissionRepository.findByAdminIdAndActiveTrue(adminId);
        return permission.map(AdminPermission::getPermissions).orElse(new HashSet<>());
    }

    public void updateAdminPermissions(Long adminId, Set<AdminPermission.PermissionType> permissions, String granterUsername) {
        AdminUser granter = adminUserRepository.findByUsername(granterUsername)
                .orElseThrow(() -> new RuntimeException("授权者不存在"));

        // 查找现有权限记录
        Optional<AdminPermission> existingPermission = permissionRepository.findByAdminIdAndActiveTrue(adminId);

        if (existingPermission.isPresent()) {
            // 更新现有权限
            AdminPermission permission = existingPermission.get();
            permission.setPermissions(permissions != null ? permissions : new HashSet<>());
            permission.setGrantedBy(granter.getId());
            permission.setGrantedAt(LocalDateTime.now());
            permissionRepository.save(permission);
        } else {
            // 创建新权限记录
            AdminPermission permission = new AdminPermission();
            permission.setAdminId(adminId);
            permission.setPermissions(permissions != null ? permissions : new HashSet<>());
            permission.setGrantedBy(granter.getId());
            permission.setActive(true);
            permissionRepository.save(permission);
        }
    }

    public boolean hasPermission(Long adminId, AdminPermission.PermissionType permissionType) {
        // 超级管理员拥有所有权限
        Optional<AdminUser> admin = adminUserRepository.findById(adminId);
        if (admin.isPresent() && admin.get().getRole() == AdminUser.AdminRole.SUPER_ADMIN) {
            return true;
        }

        Set<AdminPermission.PermissionType> permissions = getAdminPermissions(adminId);
        return permissions.contains(permissionType);
    }

    public void revokeAllPermissions(Long adminId) {
        Optional<AdminPermission> permission = permissionRepository.findByAdminIdAndActiveTrue(adminId);
        if (permission.isPresent()) {
            AdminPermission p = permission.get();
            p.setActive(false);
            permissionRepository.save(p);
        }
    }

    // 获取默认管理员权限
    public Set<AdminPermission.PermissionType> getDefaultAdminPermissions() {
        Set<AdminPermission.PermissionType> defaultPermissions = new HashSet<>();
        defaultPermissions.add(AdminPermission.PermissionType.USER_VIEW);
        defaultPermissions.add(AdminPermission.PermissionType.VIDEO_VIEW);
        defaultPermissions.add(AdminPermission.PermissionType.VIDEO_APPROVE);
        defaultPermissions.add(AdminPermission.PermissionType.COMMENT_VIEW);
        defaultPermissions.add(AdminPermission.PermissionType.COMMENT_DELETE);
        defaultPermissions.add(AdminPermission.PermissionType.CONTENT_STATISTICS);
        return defaultPermissions;
    }
}