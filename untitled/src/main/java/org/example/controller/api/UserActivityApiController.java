package org.example.controller.api;

import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户活动API控制器
 */
@RestController
@RequestMapping("/api/user-activity")
public class UserActivityApiController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户等级信息
     */
    @GetMapping("/level")
    public ResponseEntity<Map<String, Object>> getCurrentUserLevel(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null || authentication.getName() == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.ok(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            
            // 计算用户等级信息
            Map<String, Object> levelInfo = calculateUserLevel(user);
            
            response.put("success", true);
            response.put("data", levelInfo);
            
        } catch (Exception e) {
            System.err.println("获取用户等级失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "获取等级信息失败");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户活动统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserActivityStats(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null || authentication.getName() == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.ok(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            
            // 计算用户活动统计
            Map<String, Object> stats = calculateActivityStats(user);
            
            response.put("success", true);
            response.put("data", stats);
            
        } catch (Exception e) {
            System.err.println("获取用户活动统计失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "获取活动统计失败");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 计算用户等级信息
     */
    private Map<String, Object> calculateUserLevel(User user) {
        Map<String, Object> levelInfo = new HashMap<>();
        
        // 基础经验值（可以根据用户活动计算）
        int baseExp = 0;
        
        // 根据用户注册时间计算经验
        if (user.getCreatedAt() != null) {
            long daysSinceRegistration = java.time.Duration.between(
                user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                java.time.Instant.now()
            ).toDays();
            baseExp += (int) daysSinceRegistration * 10; // 每天10经验
        }
        
        // 根据用户活动计算等级
        int level = calculateLevel(baseExp);
        int currentLevelExp = getLevelExp(level);
        int nextLevelExp = getLevelExp(level + 1);
        int currentExp = baseExp - currentLevelExp;
        int expToNext = nextLevelExp - baseExp;
        
        levelInfo.put("level", level);
        levelInfo.put("currentExp", Math.max(0, currentExp));
        levelInfo.put("expToNext", Math.max(0, expToNext));
        levelInfo.put("totalExp", baseExp);
        levelInfo.put("levelName", getLevelName(level));
        levelInfo.put("progress", calculateProgress(baseExp, currentLevelExp, nextLevelExp));
        
        return levelInfo;
    }

    /**
     * 计算用户活动统计
     */
    private Map<String, Object> calculateActivityStats(User user) {
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计信息
        stats.put("userId", user.getId());
        stats.put("username", user.getUsername());
        stats.put("registrationDate", user.getCreatedAt());
        
        // 计算注册天数
        if (user.getCreatedAt() != null) {
            long daysSinceRegistration = java.time.Duration.between(
                user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                java.time.Instant.now()
            ).toDays();
            stats.put("daysSinceRegistration", daysSinceRegistration);
        } else {
            stats.put("daysSinceRegistration", 0);
        }
        
        // 活动统计（这里可以根据实际需求添加更多统计）
        stats.put("totalLogins", 0); // 可以从日志中统计
        stats.put("totalPosts", 0);  // 可以从帖子表中统计
        stats.put("totalComments", 0); // 可以从评论表中统计
        stats.put("lastLoginDate", null); // 可以从日志中获取
        
        return stats;
    }

    /**
     * 根据经验值计算等级
     */
    private int calculateLevel(int exp) {
        if (exp < 100) return 1;
        if (exp < 300) return 2;
        if (exp < 600) return 3;
        if (exp < 1000) return 4;
        if (exp < 1500) return 5;
        if (exp < 2100) return 6;
        if (exp < 2800) return 7;
        if (exp < 3600) return 8;
        if (exp < 4500) return 9;
        if (exp < 5500) return 10;
        
        // 10级以上每1000经验升一级
        return 10 + (exp - 5500) / 1000;
    }

    /**
     * 获取指定等级所需的经验值
     */
    private int getLevelExp(int level) {
        if (level <= 1) return 0;
        if (level == 2) return 100;
        if (level == 3) return 300;
        if (level == 4) return 600;
        if (level == 5) return 1000;
        if (level == 6) return 1500;
        if (level == 7) return 2100;
        if (level == 8) return 2800;
        if (level == 9) return 3600;
        if (level == 10) return 4500;
        if (level == 11) return 5500;
        
        // 11级以上每级需要额外1000经验
        return 5500 + (level - 11) * 1000;
    }

    /**
     * 获取等级名称
     */
    private String getLevelName(int level) {
        if (level <= 1) return "新手";
        if (level <= 3) return "初级用户";
        if (level <= 5) return "活跃用户";
        if (level <= 7) return "资深用户";
        if (level <= 10) return "专家用户";
        if (level <= 15) return "大师用户";
        if (level <= 20) return "传奇用户";
        return "至尊用户";
    }

    /**
     * 计算当前等级进度百分比
     */
    private double calculateProgress(int totalExp, int currentLevelExp, int nextLevelExp) {
        if (nextLevelExp <= currentLevelExp) return 100.0;
        
        int expInCurrentLevel = totalExp - currentLevelExp;
        int expNeededForLevel = nextLevelExp - currentLevelExp;
        
        if (expNeededForLevel <= 0) return 100.0;
        
        return Math.min(100.0, (double) expInCurrentLevel / expNeededForLevel * 100.0);
    }
}
