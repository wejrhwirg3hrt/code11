package org.example.controller;

import org.example.entity.AdminUser;
import org.example.service.AdminUserService;
import org.example.service.UserLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserLoginLogService loginLogService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误，请重试");
        }
        return "admin/login";
    }

    @GetMapping("/init")
    @ResponseBody
    public String initDefaultAdmin() {
        try {
            adminUserService.createDefaultSuperAdmin();
            return "默认超级管理员创建成功！用户名: superadmin, 密码: superadmin";
        } catch (Exception e) {
            return "创建失败: " + e.getMessage();
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        try {
            // 查找管理员用户
            Optional<AdminUser> adminOpt = adminUserService.findByUsername(username);

            if (adminOpt.isEmpty()) {
                System.out.println("DEBUG: 管理员用户不存在: " + username);
                redirectAttributes.addFlashAttribute("error", "管理员账号不存在");
                return "redirect:/admin/login";
            }

            AdminUser admin = adminOpt.get();
            System.out.println("DEBUG: 找到管理员用户: " + admin.getUsername() + ", enabled: " + admin.getEnabled() + ", banned: " + admin.getBanned());

            // 检查账号状态
            if (admin.getEnabled() == null || !admin.getEnabled()) {
                System.out.println("DEBUG: 账号被禁用: " + admin.getUsername());
                redirectAttributes.addFlashAttribute("error", "账号已被禁用，请联系系统管理员");
                return "redirect:/admin/login";
            }

            if (admin.getBanned() != null && admin.getBanned()) {
                System.out.println("DEBUG: 账号被封禁: " + admin.getUsername());
                redirectAttributes.addFlashAttribute("error", "账号已被封禁：" + admin.getBanReason());
                return "redirect:/admin/login";
            }

            // 验证密码
            System.out.println("DEBUG: 验证密码 for " + admin.getUsername());
            if (!passwordEncoder.matches(password, admin.getPassword())) {
                System.out.println("DEBUG: 密码验证失败 for " + admin.getUsername());
                redirectAttributes.addFlashAttribute("error", "密码错误，请重试");
                return "redirect:/admin/login";
            }
            System.out.println("DEBUG: 密码验证成功 for " + admin.getUsername());

            // 登录成功，设置会话
            session.setAttribute("adminUser", admin);
            session.setAttribute("adminRole", admin.getRole().toString());

            // 记录登录信息
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            adminUserService.updateLoginInfo(admin, clientIp);

            // 记录登录日志
            loginLogService.recordLogin(admin.getId(), admin.getUsername(), clientIp, userAgent);

            // 根据角色跳转到不同页面
            if (admin.getRole() == AdminUser.AdminRole.SUPER_ADMIN) {
                return "redirect:/super-admin/dashboard";
            } else {
                return "redirect:/admin/dashboard";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "登录失败：" + e.getMessage());
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "已安全退出管理后台");
        return "redirect:/admin/login";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}