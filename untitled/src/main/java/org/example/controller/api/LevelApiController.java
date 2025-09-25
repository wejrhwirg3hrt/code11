package org.example.controller.api;

import org.example.service.LevelService;
import org.example.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/level")
public class LevelApiController {

    @Autowired
    private LevelService levelService;

    @Autowired
    private UserActivityService userActivityService;

    /**
     * 获取用户等级信息
     */
    @GetMapping("/level")
    public ResponseEntity<Map<String, Object>> getUserLevel() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.ok(createErrorResponse("请先登录"));
            }

            String username = auth.getName();
            Long userId = userActivityService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.ok(createErrorResponse("用户不存在"));
            }

            Map<String, Object> levelInfo = userActivityService.getUserLevelInfo(userId);
            
            return ResponseEntity.ok(createSuccessResponse(levelInfo));
        } catch (Exception e) {
            return ResponseEntity.ok(createErrorResponse("获取等级信息失败: " + e.getMessage()));
        }
    }

    /**
     * 每日签到
     */
    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> dailyCheckin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.ok(createErrorResponse("请先登录"));
            }

            String username = auth.getName();
            Long userId = userActivityService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.ok(createErrorResponse("用户不存在"));
            }

            Map<String, Object> result = levelService.dailyCheckin(userId);
            
            return ResponseEntity.ok(createSuccessResponse(result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(createErrorResponse("签到失败: " + e.getMessage()));
        }
    }

    /**
     * 获取签到状态
     */
    @GetMapping("/checkin/status")
    public ResponseEntity<Map<String, Object>> getCheckinStatus() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.ok(createErrorResponse("请先登录"));
            }

            String username = auth.getName();
            Long userId = userActivityService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.ok(createErrorResponse("用户不存在"));
            }

            Map<String, Object> status = levelService.getCheckinStatus(userId);
            
            return ResponseEntity.ok(createSuccessResponse(status));
        } catch (Exception e) {
            return ResponseEntity.ok(createErrorResponse("获取签到状态失败: " + e.getMessage()));
        }
    }

    /**
     * 获取每日任务
     */
    @GetMapping("/daily-tasks")
    public ResponseEntity<Map<String, Object>> getDailyTasks() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.ok(createErrorResponse("请先登录"));
            }

            String username = auth.getName();
            Long userId = userActivityService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.ok(createErrorResponse("用户不存在"));
            }

            List<Map<String, Object>> tasks = levelService.getDailyTasks(userId);
            
            return ResponseEntity.ok(createSuccessResponse(tasks));
        } catch (Exception e) {
            return ResponseEntity.ok(createErrorResponse("获取每日任务失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户等级统计信息
     */
    @GetMapping("/level-stats")
    public ResponseEntity<Map<String, Object>> getUserLevelStats() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.ok(createErrorResponse("请先登录"));
            }

            String username = auth.getName();
            Long userId = userActivityService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.ok(createErrorResponse("用户不存在"));
            }

            Map<String, Object> stats = userActivityService.getUserActivityStats(userId);
            
            return ResponseEntity.ok(createSuccessResponse(stats));
        } catch (Exception e) {
            return ResponseEntity.ok(createErrorResponse("获取用户统计失败: " + e.getMessage()));
        }
    }

    // 辅助方法
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
