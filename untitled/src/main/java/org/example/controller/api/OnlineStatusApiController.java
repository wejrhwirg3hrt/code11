package org.example.controller.api;

import org.example.service.UserOnlineStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 在线状态API控制器
 * 为视频主页提供在线状态相关的API
 */
@RestController
@RequestMapping("/api/online-users")
public class OnlineStatusApiController {

    @Autowired
    private UserOnlineStatusService userOnlineStatusService;

    /**
     * 获取在线用户数量
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getOnlineCount() {
        try {
            int onlineCount = userOnlineStatusService.getOnlineUserCount();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("onlineCount", onlineCount);
            result.put("timestamp", System.currentTimeMillis());

            System.out.println("视频主页获取在线用户数量API被调用，当前在线用户数: " + onlineCount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取在线用户数量失败: " + e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取在线用户数量失败");
            result.put("onlineCount", 0);
            
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 获取在线用户列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getOnlineUserList() {
        try {
            Set<Long> onlineUserIds = userOnlineStatusService.getOnlineUserIds();
            int onlineCount = userOnlineStatusService.getOnlineUserCount();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("onlineUsers", onlineUserIds);
            result.put("onlineCount", onlineCount);
            result.put("timestamp", System.currentTimeMillis());

            System.out.println("视频主页获取在线用户列表API被调用，在线用户: " + onlineUserIds + "，总数: " + onlineCount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取在线用户列表失败: " + e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取在线用户列表失败");
            result.put("onlineUsers", new java.util.ArrayList<>());
            result.put("onlineCount", 0);
            
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 检查特定用户是否在线
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOnlineStatus(@PathVariable Long userId) {
        try {
            boolean isOnline = userOnlineStatusService.isUserOnline(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("isOnline", isOnline);
            result.put("status", isOnline ? "online" : "offline");
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("检查用户在线状态失败: " + e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "检查用户在线状态失败");
            result.put("userId", userId);
            result.put("isOnline", false);
            result.put("status", "offline");
            
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 批量检查用户在线状态
     */
    @PostMapping("/status/batch")
    public ResponseEntity<Map<String, Object>> getBatchUserOnlineStatus(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("批量检查用户在线状态API被调用，请求数据: " + request);

            Object userIdsObj = request.get("userIds");
            if (userIdsObj == null) {
                System.out.println("userIds为null");
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("userStatuses", new HashMap<>());
                result.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(result);
            }

            java.util.List<Long> userIds = new java.util.ArrayList<>();
            if (userIdsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> rawList = (java.util.List<Object>) userIdsObj;
                for (Object obj : rawList) {
                    if (obj instanceof Number) {
                        userIds.add(((Number) obj).longValue());
                    }
                }
            }

            System.out.println("解析后的用户ID列表: " + userIds);

            if (userIds.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("userStatuses", new HashMap<>());
                result.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(result);
            }

            Map<Long, Boolean> userStatuses = new HashMap<>();
            for (Long userId : userIds) {
                boolean isOnline = userOnlineStatusService.isUserOnline(userId);
                userStatuses.put(userId, isOnline);
                System.out.println("用户ID: " + userId + ", 在线状态: " + isOnline);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userStatuses", userStatuses);
            result.put("timestamp", System.currentTimeMillis());

            System.out.println("批量在线状态检查完成，返回结果: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("批量检查用户在线状态失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量检查用户在线状态失败: " + e.getMessage());
            result.put("userStatuses", new HashMap<>());

            return ResponseEntity.status(500).body(result);
        }
    }
}
