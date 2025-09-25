package org.example.controller;

import org.example.entity.Notification;
import org.example.entity.User;
import org.example.service.NotificationService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通知控制器
 */
@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    /**
     * 通知页面
     */
    @GetMapping("/notifications")
    public String notificationsPage(Model model, Authentication authentication,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        Page<Notification> notifications = notificationService.getUserNotifications(user.getId(), page, size);
        long unreadCount = notificationService.getUnreadCount(user.getId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifications.getTotalPages());

        return "notifications";
    }

    /**
     * 获取未读通知数量 - API
     */
    @GetMapping("/api/notifications/unread-count-legacy")
    @ResponseBody
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(0L);
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (!userOpt.isPresent()) {
            return ResponseEntity.ok(0L);
        }

        long count = notificationService.getUnreadCount(userOpt.get().getId());
        return ResponseEntity.ok(count);
    }

    /**
     * 标记通知为已读 - API
     */
    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id, 
                                                         Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.ok(response);
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }

        try {
            notificationService.markAsRead(id, userOpt.get().getId());
            response.put("success", true);
            response.put("message", "标记成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "标记失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 标记所有通知为已读 - API
     */
    @PostMapping("/api/notifications/mark-all-as-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.ok(response);
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }

        try {
            notificationService.markAllAsRead(userOpt.get().getId());
            response.put("success", true);
            response.put("message", "全部标记成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "标记失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 删除通知 - API
     */
    @DeleteMapping("/api/notifications/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id, 
                                                                 Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.ok(response);
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }

        try {
            notificationService.deleteNotification(id, userOpt.get().getId());
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 发送测试通知 - API（仅用于测试）
     */
    @PostMapping("/api/notifications/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendTestNotification(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.ok(response);
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }

        try {
            notificationService.sendSystemNotification(
                userOpt.get().getId(),
                "测试通知",
                "这是一条测试通知，用于验证通知系统是否正常工作。"
            );
            response.put("success", true);
            response.put("message", "测试通知发送成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发送失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
