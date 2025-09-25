package org.example.controller.api;

import org.example.entity.User;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/achievement-trigger")
public class AchievementTriggerController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private UserLevelService userLevelService;
    
    @Autowired
    private UserLoginLogService userLoginLogService;

    /**
     * 测试所有成就触发器
     */
    @PostMapping("/test-all")
    public ResponseEntity<Map<String, Object>> testAllTriggers(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 1. 同步用户统计数据
            userLevelService.syncUserStats(user);
            
            // 2. 触发各种成就检查
            Map<String, String> triggerResults = new HashMap<>();
            
            // 注册成就
            try {
                achievementService.triggerAchievementCheck(user, "REGISTER", 1);
                triggerResults.put("注册成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("注册成就", "❌ " + e.getMessage());
            }
            
            // 视频上传成就
            try {
                achievementService.triggerAchievementCheck(user, "UPLOAD_VIDEO", 1);
                triggerResults.put("视频上传成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("视频上传成就", "❌ " + e.getMessage());
            }
            
            // 点赞成就
            try {
                achievementService.triggerAchievementCheck(user, "LIKE_VIDEO", 1);
                triggerResults.put("点赞成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("点赞成就", "❌ " + e.getMessage());
            }
            
            // 获得点赞成就
            try {
                achievementService.triggerAchievementCheck(user, "RECEIVE_LIKE", 1);
                triggerResults.put("获得点赞成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("获得点赞成就", "❌ " + e.getMessage());
            }
            
            // 评论成就
            try {
                achievementService.triggerAchievementCheck(user, "COMMENT", 1);
                triggerResults.put("评论成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("评论成就", "❌ " + e.getMessage());
            }
            
            // 关注成就
            try {
                achievementService.triggerAchievementCheck(user, "FOLLOW_USER", 1);
                triggerResults.put("关注成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("关注成就", "❌ " + e.getMessage());
            }
            
            // 获得关注成就
            try {
                achievementService.triggerAchievementCheck(user, "RECEIVE_FOLLOW", 1);
                triggerResults.put("获得关注成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("获得关注成就", "❌ " + e.getMessage());
            }
            
            // 观看视频成就
            try {
                achievementService.triggerAchievementCheck(user, "WATCH_VIDEO", 1);
                triggerResults.put("观看视频成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("观看视频成就", "❌ " + e.getMessage());
            }
            
            // 观看时长成就
            try {
                achievementService.triggerAchievementCheck(user, "WATCH_TIME", 3600);
                triggerResults.put("观看时长成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("观看时长成就", "❌ " + e.getMessage());
            }
            
            // 登录成就
            try {
                achievementService.triggerAchievementCheck(user, "LOGIN", 1);
                triggerResults.put("登录成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("登录成就", "❌ " + e.getMessage());
            }
            
            // 连续登录成就
            try {
                achievementService.triggerAchievementCheck(user, "CONSECUTIVE_DAYS", 7);
                triggerResults.put("连续登录成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("连续登录成就", "❌ " + e.getMessage());
            }
            
            // 等级成就
            try {
                achievementService.triggerAchievementCheck(user, "LEVEL_UP", 5);
                triggerResults.put("等级成就", "✅ 已触发");
            } catch (Exception e) {
                triggerResults.put("等级成就", "❌ " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "所有成就触发器测试完成");
            response.put("triggerResults", triggerResults);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "测试失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 模拟登录触发器
     */
    @PostMapping("/simulate-login")
    public ResponseEntity<Map<String, Object>> simulateLogin(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 模拟登录记录
            userLoginLogService.recordLogin(user.getId(), user.getUsername(), "127.0.0.1", "Test Browser");
            
            response.put("success", true);
            response.put("message", "登录触发器已执行，请检查成就和通知");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "模拟登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
