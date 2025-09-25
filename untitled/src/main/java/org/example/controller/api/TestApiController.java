package org.example.controller.api;

import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 测试页面专用API控制器
 * 不需要特殊认证，用于测试功能
 */
@RestController
@RequestMapping("/api/test")
public class TestApiController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息（用于测试页面）
     */
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUserForTest() {
        try {
            // 从SecurityContext获取认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户未登录");
                response.put("authenticated", false);
                return ResponseEntity.status(401).body(response);
            }

            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                response.put("authenticated", false);
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("authenticated", true);
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("avatar", user.getAvatar());
            response.put("email", user.getEmail());
            response.put("enabled", user.isEnabled());
            response.put("banned", user.isBanned());

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
            response.put("authenticated", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 检查认证状态
     */
    @GetMapping("/auth-status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasAuthentication", authentication != null);
        response.put("isAuthenticated", authentication != null && authentication.isAuthenticated());
        response.put("principal", authentication != null ? authentication.getPrincipal().toString() : null);
        response.put("name", authentication != null ? authentication.getName() : null);
        response.put("authorities", authentication != null ? authentication.getAuthorities().toString() : null);
        
        return ResponseEntity.ok(response);
    }
}
