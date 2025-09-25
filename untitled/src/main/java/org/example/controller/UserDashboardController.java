package org.example.controller;

import org.example.entity.User;
import org.example.entity.UserLevel;
import org.example.repository.UserAchievementRepository;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户仪表板控制器
 */
@Controller
@RequestMapping("/dashboard")
public class UserDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private AchievementService achievementService;



    /**
     * 用户仪表板页面
     */
    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }
        User user = userOpt.get();

        // 获取用户等级信息
        UserLevel userLevel = userLevelService.getUserLevel(user);
        model.addAttribute("userLevel", userLevel);
        model.addAttribute("user", user);

        return "dashboard/index";
    }

    /**
     * 获取用户等级数据API
     */
    @GetMapping("/api/level")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserLevelData(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        UserLevel userLevel = userLevelService.getUserLevel(user);
        
        Map<String, Object> data = new HashMap<>();
        data.put("level", userLevel.getLevel());
        data.put("levelTitle", userLevel.getLevelTitle());
        data.put("experiencePoints", userLevel.getExperiencePoints());
        data.put("nextLevelExp", userLevel.getNextLevelExp());
        data.put("levelProgress", Math.round(userLevel.getLevelProgress() * 10.0) / 10.0);
        
        // 计算到下一级还需要的经验
        long expToNext = userLevel.getNextLevelExp() - userLevel.getExperiencePoints();
        data.put("expToNext", Math.max(0, expToNext));
        
        return ResponseEntity.ok(data);
    }

    /**
     * 获取用户统计数据API
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserStats(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = getUserFromDetails(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        // 先同步用户统计数据
        userLevelService.syncUserStats(user);

        UserLevel userLevel = userLevelService.getUserLevel(user);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVideosUploaded", userLevel.getTotalVideosUploaded());
        stats.put("totalLikesReceived", userLevel.getTotalLikesReceived());
        stats.put("totalCommentsMade", userLevel.getTotalCommentsMade());
        stats.put("totalWatchTime", userLevel.getTotalWatchTime());
        stats.put("totalWatchTimeHours", Math.round(userLevel.getTotalWatchTime() / 3600.0 * 10.0) / 10.0);
        stats.put("consecutiveDays", userLevel.getConsecutiveDays());

        // 添加成就统计
        long totalAchievements = userAchievementRepository.countByUser(user);
        stats.put("totalAchievements", totalAchievements);

        // 添加任务统计（暂时设为0，后续可以添加任务系统）
        stats.put("totalTasks", 0);

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取观看趋势数据API
     */
    @GetMapping("/api/watch-trends")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWatchTrends(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "7") int days) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = getUserFromDetails(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        // 生成模拟的观看趋势数据
        Map<String, Object> trends = generateWatchTrends(user.getId(), days);
        
        return ResponseEntity.ok(trends);
    }

    /**
     * 获取视频状态分布API
     */
    @GetMapping("/api/video-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVideoStatusDistribution(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = getUserFromDetails(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        // 生成模拟的视频状态分布数据
        Map<String, Object> distribution = new HashMap<>();
        
        // 模拟数据
        List<Map<String, Object>> data = Arrays.asList(
            Map.of("name", "已发布", "value", 85, "color", "#28a745"),
            Map.of("name", "审核中", "value", 10, "color", "#ffc107"),
            Map.of("name", "草稿", "value", 5, "color", "#6c757d")
        );
        
        distribution.put("data", data);
        distribution.put("total", 100);
        
        return ResponseEntity.ok(distribution);
    }

    /**
     * 获取用户成就数据API
     */
    @GetMapping("/api/achievements")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserAchievements(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = getUserFromDetails(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        // 生成模拟的成就数据
        Map<String, Object> achievements = generateMockAchievements();
        
        return ResponseEntity.ok(achievements);
    }

    /**
     * 生成观看趋势数据
     */
    private Map<String, Object> generateWatchTrends(Long userId, int days) {
        List<String> labels = new ArrayList<>();
        List<Integer> viewCounts = new ArrayList<>();
        List<Integer> likeCounts = new ArrayList<>();
        List<Integer> commentCounts = new ArrayList<>();
        
        Random random = new Random();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            labels.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            
            // 生成模拟数据，最近几天数据更高
            int baseViews = Math.max(1, 20 - i * 2);
            viewCounts.add(baseViews + random.nextInt(10));
            
            int baseLikes = Math.max(0, 15 - i * 2);
            likeCounts.add(baseLikes + random.nextInt(8));
            
            int baseComments = Math.max(0, 8 - i);
            commentCounts.add(baseComments + random.nextInt(5));
        }
        
        Map<String, Object> trends = new HashMap<>();
        trends.put("labels", labels);
        trends.put("datasets", Arrays.asList(
            Map.of(
                "label", "观看量",
                "data", viewCounts,
                "borderColor", "#007bff",
                "backgroundColor", "rgba(0, 123, 255, 0.1)",
                "tension", 0.4
            ),
            Map.of(
                "label", "点赞量",
                "data", likeCounts,
                "borderColor", "#dc3545",
                "backgroundColor", "rgba(220, 53, 69, 0.1)",
                "tension", 0.4
            ),
            Map.of(
                "label", "评论量",
                "data", commentCounts,
                "borderColor", "#28a745",
                "backgroundColor", "rgba(40, 167, 69, 0.1)",
                "tension", 0.4
            )
        ));
        
        return trends;
    }

    /**
     * 生成模拟成就数据
     */
    private Map<String, Object> generateMockAchievements() {
        List<Map<String, Object>> recentAchievements = Arrays.asList(
            Map.of(
                "id", 1,
                "name", "首无成就",
                "description", "继续努力创作吧！",
                "icon", "🏆",
                "rarity", "COMMON",
                "unlockedAt", LocalDateTime.now().minusDays(1).toString()
            ),
            Map.of(
                "id", 2,
                "name", "内容创作者",
                "description", "上传了10个视频",
                "icon", "📹",
                "rarity", "UNCOMMON",
                "unlockedAt", LocalDateTime.now().minusDays(3).toString()
            ),
            Map.of(
                "id", 3,
                "name", "受人喜爱",
                "description", "获得了100个点赞",
                "icon", "❤️",
                "rarity", "RARE",
                "unlockedAt", LocalDateTime.now().minusDays(7).toString()
            )
        );
        
        Map<String, Object> achievements = new HashMap<>();
        achievements.put("recent", recentAchievements);
        achievements.put("totalUnlocked", 15);
        achievements.put("totalAchievements", 25);
        achievements.put("completionRate", 60.0);
        
        return achievements;
    }

    /**
     * 从UserDetails获取User对象的辅助方法
     */
    private User getUserFromDetails(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        return userOpt.orElse(null);
    }
}
