package org.example.controller.api;

import org.example.entity.User;
import org.example.service.NotificationService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 全局通知控制器
 */
@RestController
@RequestMapping("/api/notifications")
public class GlobalNotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("count", 0);
                response.put("hasUnread", false);
                return ResponseEntity.ok(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("count", 0);
                response.put("hasUnread", false);
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            long unreadCount = notificationService.getUnreadNotificationCount(user.getId());
            
            response.put("count", unreadCount);
            response.put("hasUnread", unreadCount > 0);
            response.put("success", true);

        } catch (Exception e) {
            System.err.println("获取未读通知数量失败: " + e.getMessage());
            response.put("count", 0);
            response.put("hasUnread", false);
            response.put("success", false);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取最新通知摘要
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getNotificationSummary(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(401).body(response);
            }

            User user = userOpt.get();
            
            // 获取未读通知数量
            long unreadCount = notificationService.getUnreadNotificationCount(user.getId());
            
            // 获取最新的几条通知
            var recentNotifications = notificationService.getRecentNotifications(user.getId(), 5);
            
            response.put("success", true);
            response.put("unreadCount", unreadCount);
            response.put("hasUnread", unreadCount > 0);
            response.put("recentNotifications", recentNotifications);

        } catch (Exception e) {
            System.err.println("获取通知摘要失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(401).body(response);
            }

            User user = userOpt.get();
            notificationService.markAllNotificationsAsRead(user.getId());
            
            response.put("success", true);
            response.put("message", "所有通知已标记为已读");

        } catch (Exception e) {
            System.err.println("标记所有通知为已读失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
