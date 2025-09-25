package org.example.service;

import org.example.entity.AdminUser;
import org.example.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createDefaultSuperAdmin() {
        if (!adminUserRepository.existsByUsername("superadmin")) {
            AdminUser superAdmin = new AdminUser();
            superAdmin.setUsername("superadmin");
            superAdmin.setEmail("superadmin@example.com");
            superAdmin.setPassword(passwordEncoder.encode("superadmin"));
            superAdmin.setRole(AdminUser.AdminRole.SUPER_ADMIN);
            superAdmin.setEnabled(true);
            superAdmin.setBanned(false);
            adminUserRepository.save(superAdmin);
        }
    }

    public Optional<AdminUser> findByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    public AdminUser authenticate(String username, String password) {
        Optional<AdminUser> adminOpt = findByUsername(username);
        if (adminOpt.isPresent()) {
            AdminUser admin = adminOpt.get();
            if (admin.getEnabled() && !admin.getBanned() &&
                    passwordEncoder.matches(password, admin.getPassword())) {

                // 更新登录时间
                admin.setLastLogin(LocalDateTime.now());
                adminUserRepository.save(admin);

                return admin;
            }
        }
        return null;
    }

    public void updateLoginInfo(AdminUser admin, String loginIp) {
        admin.setLastLogin(LocalDateTime.now());
        admin.setLoginIp(loginIp);
        adminUserRepository.save(admin);
    }
}