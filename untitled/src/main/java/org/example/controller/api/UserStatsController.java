package org.example.controller.api;

import org.example.entity.User;
import org.example.service.UserService;
import org.example.service.UserLevelService;
import org.example.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user-stats")
public class UserStatsController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserLevelService userLevelService;
    
    @Autowired
    private AchievementService achievementService;

    /**
     * 同步当前用户的统计数据并重新检查成就
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncCurrentUserStats(Authentication authentication) {
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

            // 同步统计数据
            userLevelService.syncUserStats(user);
            
            // 重新检查所有成就
            achievementService.triggerAchievementCheck(user, "SYNC_CHECK", 1);
            
            response.put("success", true);
            response.put("message", "统计数据同步完成，成就已重新检查");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "同步失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 管理员接口：同步所有用户的统计数据
     */
    @PostMapping("/sync-all")
    public ResponseEntity<Map<String, Object>> syncAllUsersStats(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user == null || !user.isAdmin()) {
                response.put("success", false);
                response.put("message", "权限不足");
                return ResponseEntity.status(403).body(response);
            }

            // 同步所有用户的统计数据
            userLevelService.syncAllUsersStats();
            
            response.put("success", true);
            response.put("message", "所有用户统计数据同步完成");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量同步失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
