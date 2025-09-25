package org.example.controller.api;

import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class UserAccountApiController {

    @Autowired
    private UserService userService;

    // API: 用户注销账号
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @RequestParam String password,
            @RequestParam String reason,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            userService.selfDeleteAccount(authentication.getName(), password, reason);
            response.put("success", true);
            response.put("message", "账号注销成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "注销失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API: 检查账号状态
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAccountStatus(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            var user = userService.findActiveByUsername(authentication.getName());
            if (user.isPresent()) {
                response.put("success", true);
                response.put("active", true);
                response.put("username", user.get().getUsername());
            } else {
                response.put("success", true);
                response.put("active", false);
                response.put("message", "账号已被注销");
            }
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}