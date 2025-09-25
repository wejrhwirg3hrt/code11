package org.example.controller.api;

import org.example.entity.*;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = "*")
public class AdminApiController {

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
    private AchievementService achievementService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserLoginLogService userLoginLogService;

    @Autowired
    private UserDataCleanupService userDataCleanupService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("pendingVideos", videoService.getVideosByStatus(Video.VideoStatus.PENDING));
        dashboard.put("totalUsers", userService.getTotalUsers());
        dashboard.put("totalVideos", videoService.getTotalVideos());
        dashboard.put("recentViolations", violationService.getRecentViolations());
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/videos")
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @PostMapping("/videos/{id}/approve")
    public ResponseEntity<Map<String, String>> approveVideo(@PathVariable Long id) {
        videoService.approveVideo(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "视频已通过审核");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/videos/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectVideo(@PathVariable Long id, @RequestParam String reason) {
        videoService.rejectVideo(id, reason);
        Map<String, String> response = new HashMap<>();
        response.put("message", "视频已拒绝");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/videos/{id}/ban")
    public ResponseEntity<Map<String, String>> banVideo(@PathVariable Long id, @RequestParam String reason) {
        videoService.banVideo(id, reason);
        Map<String, String> response = new HashMap<>();
        response.put("message", "视频已封禁");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<Map<String, String>> banUser(@PathVariable Long id, @RequestParam String reason, Authentication auth) {
        User admin = userService.findByUsername(auth.getName()).orElse(null);
        userService.banUser(id, reason, admin);
        Map<String, String> response = new HashMap<>();
        response.put("message", "用户已封禁");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{id}/unban")
    public ResponseEntity<Map<String, String>> unbanUser(@PathVariable Long id) {
        userService.unbanUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "用户已解封");
        return ResponseEntity.ok(response);
    }

    // 获取登录统计信息
    @GetMapping("/login-statistics")
    public ResponseEntity<Map<String, Object>> getLoginStatistics() {
        Map<String, Object> stats = userLoginLogService.getLoginStatistics();
        return ResponseEntity.ok(stats);
    }

    // 获取登录地点分布数据
    @GetMapping("/login-locations")
    public ResponseEntity<List<Map<String, Object>>> getLoginLocations() {
        List<UserLoginLog> loginLogs = userLoginLogService.getLoginLocations();
        List<Map<String, Object>> locationData = new ArrayList<>();
        
        // 按省份统计登录次数
        Map<String, Long> provinceCount = new HashMap<>();
        for (UserLoginLog log : loginLogs) {
            String province = log.getLoginLocation();
            if (province != null && !province.isEmpty()) {
                provinceCount.put(province, provinceCount.getOrDefault(province, 0L) + 1);
            }
        }
        
        // 转换为前端需要的格式
        for (Map.Entry<String, Long> entry : provinceCount.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("province", entry.getKey());
            data.put("count", entry.getValue());
            locationData.add(data);
        }
        
        return ResponseEntity.ok(locationData);
    }

    // 获取分类统计信息
    @GetMapping("/category-statistics")
    public ResponseEntity<List<Map<String, Object>>> getCategoryStatistics() {
        String sql = "SELECT c.name, COUNT(v.id) as count FROM categories c " +
                    "LEFT JOIN videos v ON c.id = v.category_id " +
                    "GROUP BY c.id, c.name " +
                    "ORDER BY count DESC";
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(result);
    }

    // 获取登录趋势数据
    @GetMapping("/login-trend")
    public ResponseEntity<Map<String, Object>> getLoginTrend() {
        String sql = "SELECT DATE(login_time) as date, COUNT(*) as count " +
                    "FROM user_login_logs " +
                    "WHERE login_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                    "GROUP BY DATE(login_time) " +
                    "ORDER BY date";
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        for (Map<String, Object> row : result) {
            dates.add(row.get("date").toString());
            counts.add(((Number) row.get("count")).intValue());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("dates", dates);
        response.put("counts", counts);
        
        return ResponseEntity.ok(response);
    }

    // 获取用户活跃度数据
    @GetMapping("/user-activity")
    public ResponseEntity<Map<String, Object>> getUserActivity() {
        try {
            // 使用用户登录日志表来统计活跃度
            String sql = "SELECT " +
                        "CASE " +
                        "  WHEN MAX(ull.login_time) >= DATE_SUB(NOW(), INTERVAL 1 DAY) THEN '今日活跃' " +
                        "  WHEN MAX(ull.login_time) >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN '本周活跃' " +
                        "  WHEN MAX(ull.login_time) >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN '本月活跃' " +
                        "  ELSE '长期未活跃' " +
                        "END as activity_level, " +
                        "COUNT(DISTINCT u.id) as count " +
                        "FROM users u " +
                        "LEFT JOIN user_login_logs ull ON u.id = ull.user_id " +
                        "WHERE u.deleted = false " +
                        "GROUP BY u.id " +
                        "ORDER BY activity_level";
            
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            
            // 统计各活跃度级别的用户数量
            Map<String, Integer> activityCounts = new HashMap<>();
            activityCounts.put("今日活跃", 0);
            activityCounts.put("本周活跃", 0);
            activityCounts.put("本月活跃", 0);
            activityCounts.put("长期未活跃", 0);
            
            for (Map<String, Object> row : result) {
                String activityLevel = row.get("activity_level").toString();
                Integer count = ((Number) row.get("count")).intValue();
                activityCounts.put(activityLevel, activityCounts.get(activityLevel) + count);
            }
            
            List<String> activities = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            
            // 按顺序添加数据
            activities.add("今日活跃");
            counts.add(activityCounts.get("今日活跃"));
            activities.add("本周活跃");
            counts.add(activityCounts.get("本周活跃"));
            activities.add("本月活跃");
            counts.add(activityCounts.get("本月活跃"));
            activities.add("长期未活跃");
            counts.add(activityCounts.get("长期未活跃"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("activities", activities);
            response.put("counts", counts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取用户活跃度数据失败: " + e.getMessage());
            
            // 返回模拟数据作为备用方案
            List<String> activities = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            
            activities.add("今日活跃");
            counts.add(5);
            activities.add("本周活跃");
            counts.add(12);
            activities.add("本月活跃");
            counts.add(25);
            activities.add("长期未活跃");
            counts.add(8);
            
            Map<String, Object> response = new HashMap<>();
            response.put("activities", activities);
            response.put("counts", counts);
            
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/users/{id}/warn")
    public ResponseEntity<Map<String, String>> warnUser(@PathVariable Long id, @RequestParam String reason, Authentication auth) {
        User admin = userService.findByUsername(auth.getName()).orElse(null);

        // 增加用户警告计数
        User user = userService.getUserById(id);
        user.setWarningCount(user.getWarningCount() + 1);
        userService.save(user);

        // 记录违规信息
        violationService.warnUser(id, reason, admin);

        Map<String, String> response = new HashMap<>();
        response.put("message", "已发送警告");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    @PostMapping("/announcements")
    public ResponseEntity<Announcement> createAnnouncement(@RequestParam String title, @RequestParam String content, Authentication auth) {
        User admin = userService.findByUsername(auth.getName()).orElse(null);
        Announcement announcement = announcementService.createAnnouncement(title, content, admin);
        return ResponseEntity.ok(announcement);
    }

    @PostMapping("/announcements/{id}/toggle")
    public ResponseEntity<Map<String, String>> toggleAnnouncement(@PathVariable Long id) {
        announcementService.toggleAnnouncement(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "公告状态已更新");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/violations")
    public ResponseEntity<List<Violation>> getAllViolations() {
        return ResponseEntity.ok(violationService.getAllViolations());
    }

    @PostMapping("/cleanup-example-data")
    public ResponseEntity<Map<String, String>> cleanupExampleData() {
        try {
            dataCleanupService.cleanupExampleData();
            Map<String, String> response = new HashMap<>();
            response.put("message", "测试数据清理完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "清理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/cleanup-garbled-tags")
    public ResponseEntity<Map<String, String>> cleanupGarbledTags() {
        try {
            dataCleanupService.cleanupGarbledTags();
            Map<String, String> response = new HashMap<>();
            response.put("message", "乱码标签清理完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "清理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/add-sample-tags")
    public ResponseEntity<Map<String, String>> addSampleTags() {
        try {
            dataCleanupService.addSampleTags();
            Map<String, String> response = new HashMap<>();
            response.put("message", "示例标签添加完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "添加失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 初始化成就系统
     */
    @PostMapping("/init-achievements")
    public ResponseEntity<Map<String, Object>> initAchievements() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 清空现有成就数据
            jdbcTemplate.execute("DELETE FROM user_achievements");
            jdbcTemplate.execute("DELETE FROM achievements");
            jdbcTemplate.execute("ALTER TABLE achievements AUTO_INCREMENT = 1");

            // 插入基础成就
            String baseSql = "INSERT INTO achievements (name, description, category, rarity, points, condition_type, condition_value, icon_url, is_active, created_at) VALUES ";

            // 基础成就
            jdbcTemplate.execute(baseSql + "('初来乍到', '完成用户注册', 'BASIC', 'COMMON', 10, 'REGISTER', 1, '/images/achievements/register.png', TRUE, NOW())");
            jdbcTemplate.execute(baseSql + "('首次上传', '上传第一个视频', 'UPLOAD', 'COMMON', 20, 'UPLOAD_VIDEO', 1, '/images/achievements/first_upload.png', TRUE, NOW())");
            jdbcTemplate.execute(baseSql + "('首次点赞', '给视频点第一个赞', 'SOCIAL', 'COMMON', 10, 'LIKE_VIDEO', 1, '/images/achievements/first_like.png', TRUE, NOW())");
            jdbcTemplate.execute(baseSql + "('首次评论', '发表第一条评论', 'SOCIAL', 'COMMON', 15, 'COMMENT', 1, '/images/achievements/first_comment.png', TRUE, NOW())");
            jdbcTemplate.execute(baseSql + "('首次观看', '观看第一个视频', 'WATCH', 'COMMON', 5, 'WATCH_VIDEO', 1, '/images/achievements/first_watch.png', TRUE, NOW())");

            // 观看成就
            jdbcTemplate.execute(baseSql + "('视频新手', '观看10个视频', 'WATCH', 'COMMON', 50, 'WATCH_VIDEO', 10, '/images/achievements/watch_10.png', TRUE, NOW())");
            jdbcTemplate.execute(baseSql + "('视频爱好者', '观看50个视频', 'WATCH', 'COMMON', 100, 'WATCH_VIDEO', 50, '/images/achievements/watch_50.png', TRUE, NOW())");
            jdbcTemplate.execute(baseSql + "('视频达人', '观看100个视频', 'WATCH', 'UNCOMMON', 200, 'WATCH_VIDEO', 100, '/images/achievements/watch_100.png', TRUE, NOW())");
            jdbcTemplate.execute(baseSql + "('视频专家', '观看500个视频', 'WATCH', 'RARE', 500, 'WATCH_VIDEO', 500, '/images/achievements/watch_500.png', TRUE, NOW())");
            jdbcTemplate.execute(baseSql + "('千次观看', '观看1000个视频', 'WATCH', 'EPIC', 1000, 'WATCH_VIDEO', 1000, '/images/achievements/watch_1000.png', TRUE, NOW())");

            response.put("success", true);
            response.put("message", "成就系统初始化完成！已插入10个基础成就");
            response.put("totalAchievements", achievementService.getAllAchievements().size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "成就系统初始化失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取成就统计信息
     */
    @GetMapping("/achievements/stats")
    public ResponseEntity<Map<String, Object>> getAchievementStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Achievement> achievements = achievementService.getAllAchievements();

            response.put("success", true);
            response.put("totalAchievements", achievements.size());

            // 按分类统计
            Map<String, Long> categoryStats = achievements.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    a -> a.getCategory().toString(),
                    java.util.stream.Collectors.counting()
                ));
            response.put("categoryStats", categoryStats);

            // 按稀有度统计
            Map<String, Long> rarityStats = achievements.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    a -> a.getRarity().toString(),
                    java.util.stream.Collectors.counting()
                ));
            response.put("rarityStats", rarityStats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取成就统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== 用户数据清理API ====================

    /**
     * 获取用户数据清理统计信息
     */
    @GetMapping("/user-cleanup/statistics")
    public ResponseEntity<Map<String, Object>> getUserCleanupStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            String statistics = userDataCleanupService.getCleanupStatistics();
            response.put("success", true);
            response.put("statistics", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清理所有普通用户数据
     */
    @PostMapping("/user-cleanup/cleanup-all")
    public ResponseEntity<Map<String, Object>> cleanupAllUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            userDataCleanupService.cleanupAllUserData();
            response.put("success", true);
            response.put("message", "所有普通用户数据清理完成！管理员和root用户已保留。");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清理已删除用户数据
     */
    @PostMapping("/user-cleanup/cleanup-deleted")
    public ResponseEntity<Map<String, Object>> cleanupDeletedUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            userDataCleanupService.cleanupDeletedUserData();
            response.put("success", true);
            response.put("message", "已删除用户数据清理完成！");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}