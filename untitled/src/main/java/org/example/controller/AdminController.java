package org.example.controller;

import org.example.entity.*;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private ViolationService violationService;

    @Autowired
    private DataCleanupService dataCleanupService;

    @Autowired
    private LogManagementService logManagementService;

    // 管理员仪表板
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendingVideos", videoService.getVideosByStatus(Video.VideoStatus.PENDING));
        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("totalVideos", videoService.getTotalVideos());
        model.addAttribute("recentViolations", violationService.getRecentViolations());
        return "admin/dashboard";
    }

    @GetMapping("/videos")
    public String videoManagement(Model model) {
        model.addAttribute("videos", videoService.getAllVideos());
        return "admin/videos";
    }

    @PostMapping("/videos/{id}/approve")
    public String approveVideo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        videoService.approveVideo(id);
        redirectAttributes.addFlashAttribute("success", "视频已通过审核");
        return "redirect:/admin/videos";
    }

    @PostMapping("/videos/{id}/reject")
    public String rejectVideo(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirectAttributes) {
        videoService.rejectVideo(id, reason);
        redirectAttributes.addFlashAttribute("success", "视频已拒绝");
        return "redirect:/admin/videos";
    }

    @PostMapping("/videos/{id}/ban")
    public String banVideo(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirectAttributes) {
        videoService.banVideo(id, reason);
        redirectAttributes.addFlashAttribute("success", "视频已封禁");
        return "redirect:/admin/videos";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/ban")
    public String banUser(@PathVariable Long id, @RequestParam String reason,
                          Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByUsername(auth.getName()).orElse(null);
            userService.banUser(id, reason, admin);
            redirectAttributes.addFlashAttribute("success", "用户已封禁");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "封禁失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unban")
    public String unbanUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.unbanUser(id);
            redirectAttributes.addFlashAttribute("success", "用户已解封");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "解封失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/warn")
    public String warnUser(@PathVariable Long id, @RequestParam String reason,
                           Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByUsername(auth.getName()).orElse(null);

            // 增加用户警告计数
            User user = userService.getUserById(id);
            user.setWarningCount(user.getWarningCount() + 1);
            userService.save(user);

            // 记录违规信息
            violationService.warnUser(id, reason, admin);

            redirectAttributes.addFlashAttribute("success", "已发送警告");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "警告失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/announcements")
    public String announcementManagement(Model model) {
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        return "admin/announcements";
    }

    @GetMapping("/announcements/new")
    public String newAnnouncement() {
        return "admin/announcement-form";
    }

    @PostMapping("/announcements")
    public String createAnnouncement(@RequestParam String title, @RequestParam String content,
                                     Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByUsername(auth.getName()).orElse(null);
            announcementService.createAnnouncement(title, content, admin);
            redirectAttributes.addFlashAttribute("success", "公告已发布");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "发布失败: " + e.getMessage());
        }
        return "redirect:/admin/announcements";
    }

    @PostMapping("/announcements/{id}/toggle")
    public String toggleAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            announcementService.toggleAnnouncement(id);
            redirectAttributes.addFlashAttribute("success", "公告状态已更新");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        return "redirect:/admin/announcements";
    }

    // ==================== 日志管理 ====================
    @GetMapping("/logs")
    public String logManagement(Model model) {
        LogManagementService.LogStatistics stats = logManagementService.getLogStatistics();
        model.addAttribute("logStats", stats);
        return "admin/log-management";
    }

    @PostMapping("/logs/clear-all")
    @ResponseBody
    public LogManagementService.LogCleanupResult clearAllLogs() {
        return logManagementService.clearAllLogs();
    }

    @PostMapping("/logs/retain-1-day")
    @ResponseBody
    public LogManagementService.LogCleanupResult retainLogsFor1Day() {
        return logManagementService.retainLogsForDays(1);
    }

    @PostMapping("/logs/retain-5-days")
    @ResponseBody
    public LogManagementService.LogCleanupResult retainLogsFor5Days() {
        return logManagementService.retainLogsForDays(5);
    }

    @PostMapping("/logs/retain-30-days")
    @ResponseBody
    public LogManagementService.LogCleanupResult retainLogsFor30Days() {
        return logManagementService.retainLogsForDays(30);
    }

    @GetMapping("/logs/statistics")
    @ResponseBody
    public LogManagementService.LogStatistics getLogStatistics() {
        return logManagementService.getLogStatistics();
    }

    // 查看已注销用户
    @GetMapping("/deleted-users")
    public String deletedUsers(Model model) {
        model.addAttribute("deletedUsers", userService.getAllDeletedUsers());
        return "admin/deleted-users";
    }

    // 恢复已注销用户
    @PostMapping("/users/{id}/restore")
    public String restoreUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.restoreAccount(id);
            redirectAttributes.addFlashAttribute("success", "用户账号已恢复");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "恢复失败: " + e.getMessage());
        }
        return "redirect:/admin/deleted-users";
    }

    // 强制注销用户账号
    @PostMapping("/users/{id}/force-delete")
    public String forceDeleteUser(@PathVariable Long id,
                                  @RequestParam String reason,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.deleteAccount(id, "管理员强制注销：" + reason);
            redirectAttributes.addFlashAttribute("success", "用户账号已注销");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "注销失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/user-cleanup")
    public String userCleanup() {
        return "admin/user-cleanup";
    }

    @GetMapping("/violations")
    public String violationManagement(Model model) {
        model.addAttribute("violations", violationService.getAllViolations());
        return "admin/violations";
    }

    @GetMapping("/data-cleanup")
    public String dataCleanupPage() {
        return "admin/data-cleanup";
    }

    // 数据清理功能
    @PostMapping("/cleanup/example-data")
    @ResponseBody
    public String cleanupExampleData() {
        try {
            dataCleanupService.cleanupExampleData();
            return "清理完成";
        } catch (Exception e) {
            return "清理失败: " + e.getMessage();
        }
    }

    @PostMapping("/cleanup/fix-thumbnails")
    @ResponseBody
    public String fixThumbnails() {
        try {
            dataCleanupService.fixInvalidThumbnails();
            return "修复完成";
        } catch (Exception e) {
            return "修复失败: " + e.getMessage();
        }
    }

}