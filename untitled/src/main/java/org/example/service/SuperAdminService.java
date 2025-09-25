package org.example.service;

import org.example.entity.AdminUser;
import org.example.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = false)
public class SuperAdminService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    public List<AdminUser> getAllAdminUsers() {
        return adminUserRepository.findAll();
    }

    public long getTotalAdmins() {
        return adminUserRepository.count();
    }

    public AdminUser getAdminById(Long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
    }

    public AdminUser createAdmin(String username, String email, String password,
                                 AdminUser.AdminRole role, String creatorUsername) {
        // 检查用户名是否已存在
        if (adminUserRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (adminUserRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在");
        }

        // 获取创建者ID
        AdminUser creator = adminUserRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("创建者不存在"));

        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(role);
        admin.setEnabled(true);
        admin.setBanned(false);

        return adminUserRepository.save(admin);
    }

    public void banAdmin(Long adminId, String reason) {
        AdminUser admin = getAdminById(adminId);

        // 不能封禁超级管理员
        if (admin.getRole() == AdminUser.AdminRole.SUPER_ADMIN) {
            throw new RuntimeException("不能封禁超级管理员");
        }

        admin.setBanned(true);
        admin.setBanReason(reason);
        admin.setEnabled(false);
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(admin);
    }

    public void unbanAdmin(Long adminId) {
        AdminUser admin = getAdminById(adminId);
        admin.setBanned(false);
        admin.setBanReason(null);
        admin.setEnabled(true);
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(admin);
    }

    public void resetUserPassword(Long userId) {
        userService.resetPassword(userId, "123456");
    }

    public void deleteAdmin(Long adminId) {
        AdminUser admin = getAdminById(adminId);

        // 不能删除超级管理员
        if (admin.getRole() == AdminUser.AdminRole.SUPER_ADMIN) {
            throw new RuntimeException("不能删除超级管理员");
        }

        adminUserRepository.delete(admin);
    }

    public void updateAdminInfo(Long adminId, String email, AdminUser.AdminRole role) {
        AdminUser admin = getAdminById(adminId);

        if (email != null && !email.equals(admin.getEmail())) {
            if (adminUserRepository.existsByEmail(email)) {
                throw new RuntimeException("邮箱已存在");
            }
            admin.setEmail(email);
        }

        if (role != null) {
            admin.setRole(role);
        }

        admin.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(admin);
    }

}