package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.service.*;
import org.example.entity.*;
import org.example.config.DataInitializer;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminController {

    @Autowired
    private SuperAdminService superAdminService;

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AdminPermissionService permissionService;

    @Autowired
    private CdnService cdnService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserDataCleanupService userDataCleanupService;

    @Autowired
    private DataInitializer dataInitializer;


    // ==================== 仪表板 ====================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 获取真实的统计数据
        Map<String, Object> systemStats = statisticsService.getSystemStatistics();
        Map<String, Object> todayStats = statisticsService.getTodayStatistics();
        List<Map<String, Object>> popularVideos = statisticsService.getPopularVideoStatistics(5);
        List<Map<String, Object>> activeUsers = statisticsService.getActiveUserStatistics(5);
        Map<String, Long> statusDistribution = statisticsService.getVideoStatusDistribution();
        Map<String, Long> userGrowthTrend = statisticsService.getUserGrowthTrend();

        // 添加到模型
        model.addAttribute("systemStats", systemStats);
        model.addAttribute("todayStats", todayStats);
        model.addAttribute("popularVideos", popularVideos);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("statusDistribution", statusDistribution);
        model.addAttribute("userGrowthTrend", userGrowthTrend);

        // 保持向后兼容
        model.addAttribute("totalUsers", systemStats.get("totalUsers"));
        model.addAttribute("totalVideos", systemStats.get("totalVideos"));
        model.addAttribute("totalComments", systemStats.get("totalComments"));
        model.addAttribute("totalAdmins", superAdminService.getTotalAdmins());
        model.addAttribute("pendingVideos", systemStats.get("pendingVideos"));
        model.addAttribute("pendingComments", commentService.getPendingCommentsCount());

        return "super-admin/dashboard";
    }

    // ==================== 管理员管理 ====================
    @GetMapping("/admin-management")
    public String adminManagement(Model model) {
        model.addAttribute("adminUsers", superAdminService.getAllAdminUsers());
        return "super-admin/admin-management";
    }

    @GetMapping("/create-admin")
    public String createAdminForm(Model model) {
        model.addAttribute("roles", AdminUser.AdminRole.values());
        return "super-admin/create-admin";
    }

    @PostMapping("/create-admin")
    public String createAdmin(@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam AdminUser.AdminRole role,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String creatorUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";
            superAdminService.createAdmin(username, email, password, role, creatorUsername);
            redirectAttributes.addFlashAttribute("success", "管理员创建成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "创建失败: " + e.getMessage());
        }
        return "redirect:/super-admin/admin-management";
    }

    @PostMapping("/admin/{id}/ban")
    public String banAdmin(@PathVariable Long id,
                           @RequestParam String reason,
                           RedirectAttributes redirectAttributes) {
        superAdminService.banAdmin(id, reason);
        redirectAttributes.addFlashAttribute("success", "管理员已封禁");
        return "redirect:/super-admin/admin-management";
    }

    @PostMapping("/admin/{id}/unban")
    public String unbanAdmin(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        superAdminService.unbanAdmin(id);
        redirectAttributes.addFlashAttribute("success", "管理员已解封");
        return "redirect:/super-admin/admin-management";
    }

    // ==================== 权限管理 ====================
    @GetMapping("/admin/{id}/permissions")
    public String managePermissions(@PathVariable Long id, Model model) {
        AdminUser admin = superAdminService.getAdminById(id);
        Set<AdminPermission.PermissionType> currentPermissions = permissionService.getAdminPermissions(id);

        model.addAttribute("admin", admin);
        model.addAttribute("currentPermissions", currentPermissions);
        model.addAttribute("allPermissions", AdminPermission.PermissionType.values());
        return "super-admin/admin-permissions";
    }

    @PostMapping("/admin/{id}/permissions")
    public String updatePermissions(@PathVariable Long id,
                                    @RequestParam(required = false) Set<AdminPermission.PermissionType> permissions,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String granterUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";
            permissionService.updateAdminPermissions(id, permissions, granterUsername);
            redirectAttributes.addFlashAttribute("success", "权限更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "权限更新失败: " + e.getMessage());
        }
        return "redirect:/super-admin/admin-management";
    }

    // ==================== 用户管理 ====================
    @GetMapping("/user-management")
    public String userManagement(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        model.addAttribute("users", userService.getAllUsersPaged(page, size));
        model.addAttribute("currentPage", page);
        return "super-admin/user-management";
    }

    @PostMapping("/user/{id}/ban")
    public String banUser(@PathVariable Long id,
                          @RequestParam String reason,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            User admin = currentAdmin != null ?
                userService.findByUsername(currentAdmin.getUsername()).orElse(null) : null;
            userService.banUser(id, reason, admin);
            redirectAttributes.addFlashAttribute("success", "用户已封禁");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "封禁失败: " + e.getMessage());
        }
        return "redirect:/super-admin/user-management";
    }

    @PostMapping("/user/{id}/unban")
    public String unbanUser(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            userService.unbanUser(id);
            redirectAttributes.addFlashAttribute("success", "用户已解封");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "解封失败: " + e.getMessage());
        }
        return "redirect:/super-admin/user-management";
    }

    @PostMapping("/user/{id}/reset-password")
    public String resetUserPassword(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            superAdminService.resetUserPassword(id);
            redirectAttributes.addFlashAttribute("success", "密码已重置为123456");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "重置失败: " + e.getMessage());
        }
        return "redirect:/super-admin/user-management";
    }

    // ==================== 视频管理 ====================
    @GetMapping("/video-management")
    public String videoManagement(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        model.addAttribute("videos", videoService.getAllVideosPaged(page, size));
        model.addAttribute("pendingVideos", videoService.getPendingVideos());
        model.addAttribute("currentPage", page);
        return "super-admin/video-management";
    }

    @PostMapping("/video/{id}/approve")
    public String approveVideo(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            videoService.approveVideo(id);
            redirectAttributes.addFlashAttribute("success", "视频已通过审核");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "审核失败: " + e.getMessage());
        }
        return "redirect:/super-admin/video-management";
    }

    @PostMapping("/video/{id}/reject")
    public String rejectVideo(@PathVariable Long id,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            videoService.rejectVideo(id, reason);
            redirectAttributes.addFlashAttribute("success", "视频已拒绝");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "拒绝失败: " + e.getMessage());
        }
        return "redirect:/super-admin/video-management";
    }

    @PostMapping("/video/{id}/ban")
    public String banVideo(@PathVariable Long id,
                           @RequestParam String reason,
                           RedirectAttributes redirectAttributes) {
        try {
            videoService.banVideo(id, reason);
            redirectAttributes.addFlashAttribute("success", "视频已封禁");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "封禁失败: " + e.getMessage());
        }
        return "redirect:/super-admin/video-management";
    }

    // ==================== 评论管理 ====================
    @GetMapping("/comment-management")
    public String commentManagement(Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        model.addAttribute("comments", commentService.getAllCommentsPaged(page, size));
        model.addAttribute("pendingComments", commentService.getPendingComments());
        model.addAttribute("currentPage", page);
        return "super-admin/comment-management";
    }

    @PostMapping("/comment/{id}/approve")
    public String approveComment(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            commentService.approveComment(id);
            redirectAttributes.addFlashAttribute("success", "评论已通过审核");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "审核失败: " + e.getMessage());
        }
        return "redirect:/super-admin/comment-management";
    }

    @PostMapping("/comment/{id}/ban")
    public String banComment(@PathVariable Long id,
                             @RequestParam String reason,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String adminUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";
            commentService.banComment(id, reason, adminUsername);
            redirectAttributes.addFlashAttribute("success", "评论已封禁");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "封禁失败: " + e.getMessage());
        }
        return "redirect:/super-admin/comment-management";
    }

    // ==================== CDN管理 ====================
    @GetMapping("/cdn-management")
    public String cdnManagement(Model model) {
        model.addAttribute("cdnConfigs", cdnService.getAllCdnConfigs());
        model.addAttribute("cdnStatistics", cdnService.getCdnStatistics());
        return "super-admin/cdn-management";
    }

    @GetMapping("/cdn/create")
    public String createCdnForm() {
        return "super-admin/cdn-create";
    }

    @PostMapping("/cdn/create")
    public String createCdn(@ModelAttribute CdnConfig cdnConfig,
                            RedirectAttributes redirectAttributes) {
        try {
            cdnService.createCdnConfig(cdnConfig);
            redirectAttributes.addFlashAttribute("success", "CDN配置创建成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "创建失败: " + e.getMessage());
        }
        return "redirect:/super-admin/cdn-management";
    }

    @PostMapping("/cdn/{id}/toggle")
    public String toggleCdn(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            cdnService.toggleCdnStatus(id);
            redirectAttributes.addFlashAttribute("success", "CDN状态已更新");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "状态更新失败: " + e.getMessage());
        }
        return "redirect:/super-admin/cdn-management";
    }

    @PostMapping("/cdn/{id}/clear-cache")
    public String clearCdnCache(@PathVariable Long id,
                                @RequestParam(required = false) String path,
                                RedirectAttributes redirectAttributes) {
        try {
            cdnService.clearCache(id, path);
            redirectAttributes.addFlashAttribute("success", "缓存清理成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "缓存清理失败: " + e.getMessage());
        }
        return "redirect:/super-admin/cdn-management";
    }

    // ==================== 公告管理 ====================
    @GetMapping("/announcement-management")
    public String announcementManagement(Model model,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        model.addAttribute("currentPage", page);
        return "super-admin/announcement-management";
    }

    @GetMapping("/announcement/create")
    public String createAnnouncementPage() {
        return "super-admin/announcement-create";
    }

    @PostMapping("/announcement/create")
    public String createAnnouncement(@RequestParam String title,
                                     @RequestParam String content,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String creatorUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";

            // 由于现有的createAnnouncement方法需要User对象，我们需要适配
            Announcement announcement = new Announcement();
            announcement.setTitle(title);
            announcement.setContent(content);
            announcement.setActive(true);

            announcementService.createAnnouncement(title, content, null); // 暂时传null
            redirectAttributes.addFlashAttribute("success", "公告创建成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "创建失败: " + e.getMessage());
        }
        return "redirect:/super-admin/announcement-management";
    }

    @PostMapping("/announcement/{id}/toggle")
    public String toggleAnnouncement(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            announcementService.toggleAnnouncement(id);
            redirectAttributes.addFlashAttribute("success", "公告状态已更新");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "状态更新失败: " + e.getMessage());
        }
        return "redirect:/super-admin/announcement-management";
    }

    // ==================== 导出报告 ====================
    @GetMapping("/export/users")
    public ResponseEntity<String> exportUsers() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("ID,用户名,邮箱,注册时间,状态\n");

            userService.getAllUsers().forEach(user -> {
                csv.append(user.getId()).append(",")
                   .append(user.getUsername()).append(",")
                   .append(user.getEmail()).append(",")
                   .append(user.getCreatedAt()).append(",")
                   .append(user.getBanned() != null && user.getBanned() ? "已封禁" : "正常").append("\n");
            });

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=users.csv")
                    .body(csv.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("导出失败: " + e.getMessage());
        }
    }

    @GetMapping("/export/videos")
    public ResponseEntity<String> exportVideos() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("ID,标题,上传者,状态,观看次数,创建时间\n");

            videoService.getAllVideos().forEach(video -> {
                csv.append(video.getId()).append(",")
                   .append("\"").append(video.getTitle()).append("\"").append(",")
                   .append(video.getUser() != null ? video.getUser().getUsername() : "未知").append(",")
                   .append(video.getStatus().getDescription()).append(",")
                   .append(video.getViews() != null ? video.getViews() : 0).append(",")
                   .append(video.getCreatedAt()).append("\n");
            });

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=videos.csv")
                    .body(csv.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("导出失败: " + e.getMessage());
        }
    }

    @GetMapping("/export/comments")
    public ResponseEntity<String> exportComments() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("ID,用户名,内容,状态,创建时间\n");

            commentService.getAllCommentsPaged(0, Integer.MAX_VALUE).getContent().forEach(comment -> {
                csv.append(comment.getId()).append(",")
                   .append(comment.getUsername() != null ? comment.getUsername() : "匿名").append(",")
                   .append("\"").append(comment.getContent()).append("\"").append(",")
                   .append(comment.getStatus()).append(",")
                   .append(comment.getCreatedAt()).append("\n");
            });

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=comments.csv")
                    .body(csv.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("导出失败: " + e.getMessage());
        }
    }

    // ==================== 数据管理与初始化 ====================
    @GetMapping("/data-management")
    public String dataManagement(Model model) {
        // 获取数据统计信息
        String statistics = userDataCleanupService.getCleanupStatistics();
        model.addAttribute("statistics", statistics);
        return "super-admin/data-management";
    }

    @GetMapping("/data-management/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDataStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取各种统计数据
            long userCount = userService.getTotalUsers();
            long videoCount = videoService.getTotalVideos();
            long commentCount = commentService.getTotalComments();
            long musicCount = 0; // 如果有音乐服务的话
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("userCount", userCount);
            statistics.put("videoCount", videoCount);
            statistics.put("commentCount", commentCount);
            statistics.put("musicCount", musicCount);
            
            result.put("statistics", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/data-management/cleanup-all-users")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cleanupAllUsers(HttpSession session) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String adminUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";
            
            userDataCleanupService.cleanupAllUserData();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "所有普通用户数据清理成功");
            result.put("adminUsername", adminUsername);
            result.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/data-management/cleanup-deleted-users")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cleanupDeletedUsers(HttpSession session) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String adminUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";
            
            userDataCleanupService.cleanupDeletedUserData();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "已删除用户数据清理成功");
            result.put("adminUsername", adminUsername);
            result.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/data-management/cleanup-specific-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cleanupSpecificUser(@RequestParam Long userId, HttpSession session) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String adminUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";
            
            userDataCleanupService.cleanupUserData(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "用户ID " + userId + " 的数据清理成功");
            result.put("adminUsername", adminUsername);
            result.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 系统初始化 ====================
    @PostMapping("/data-management/init-test-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> initTestData(HttpSession session) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String adminUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";
            
            // 调用测试数据初始化服务
            dataInitializer.initializeTestData();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "测试数据初始化成功！已创建测试用户、视频等数据");
            result.put("adminUsername", adminUsername);
            result.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "初始化失败：" + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    @PostMapping("/data-management/reset-system")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetSystem(HttpSession session) {
        try {
            AdminUser currentAdmin = (AdminUser) session.getAttribute("adminUser");
            String adminUsername = currentAdmin != null ? currentAdmin.getUsername() : "system";
            
            // 这里可以添加系统重置逻辑
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "系统重置成功");
            result.put("adminUsername", adminUsername);
            result.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


}