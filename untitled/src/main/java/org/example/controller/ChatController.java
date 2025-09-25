package org.example.controller;

import org.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import org.example.service.UserService;

/**
 * 聊天控制器
 */
@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private UserService userService;

    /**
     * 聊天首页 - 简化版本，先显示页面再处理认证
     */
    @GetMapping
    public String chatIndex(Model model, HttpServletRequest request) {
        // 手动检查认证状态，但不强制要求
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("=== 聊天页面认证调试 ===");
        System.out.println("Authentication: " + (auth != null ? auth.getName() : "null"));
        System.out.println("IsAuthenticated: " + (auth != null ? auth.isAuthenticated() : false));
        System.out.println("Auth Type: " + (auth != null ? auth.getClass().getSimpleName() : "null"));

        // 检查是否为匿名用户或未认证
        if (auth == null || !auth.isAuthenticated() ||
            "anonymousUser".equals(auth.getName()) ||
            auth instanceof AnonymousAuthenticationToken) {
            System.out.println("用户未认证，显示匿名聊天页面");
            model.addAttribute("currentUser", null);
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("authStatus", "未认证");
            return "chat/index";
        }

        // 已认证用户，尝试加载用户数据
        try {
            User currentUser = userService.findByUsername(auth.getName()).orElse(null);
            if (currentUser == null) {
                System.out.println("找不到用户数据，显示匿名聊天页面");
                model.addAttribute("currentUser", null);
                model.addAttribute("isAuthenticated", false);
                model.addAttribute("authStatus", "用户数据未找到");
                return "chat/index";
            }

            System.out.println("用户已认证: " + currentUser.getUsername() + ", ID: " + currentUser.getId());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("authStatus", "已认证");
        } catch (Exception e) {
            System.out.println("获取用户数据时出错: " + e.getMessage());
            model.addAttribute("currentUser", null);
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("authStatus", "认证错误: " + e.getMessage());
        }

        return "chat/index";
    }

    /**
     * 聊天首页 - 支持带用户参数的跳转
     */
    @GetMapping(params = {"user", "username"})
    public String chatIndexWithUser(@RequestParam Long user, 
                                   @RequestParam String username,
                                   Model model, 
                                   HttpServletRequest request) {
        System.out.println("=== 聊天页面带用户参数跳转 ===");
        System.out.println("目标用户ID: " + user + ", 用户名: " + username);
        
        // 手动检查认证状态
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // 检查是否为匿名用户或未认证
        if (auth == null || !auth.isAuthenticated() ||
            "anonymousUser".equals(auth.getName()) ||
            auth instanceof AnonymousAuthenticationToken) {
            System.out.println("用户未认证，重定向到登录页面");
            return "redirect:/login";
        }

        // 已认证用户，加载用户数据
        try {
            User currentUser = userService.findByUsername(auth.getName()).orElse(null);
            if (currentUser == null) {
                System.out.println("找不到当前用户数据，重定向到登录页面");
                return "redirect:/login";
            }

            // 获取目标用户信息
            User targetUser = userService.findById(user).orElse(null);
            if (targetUser == null) {
                System.out.println("找不到目标用户，跳转到普通聊天页面");
                return "redirect:/chat";
            }

            System.out.println("当前用户: " + currentUser.getUsername() + ", 目标用户: " + targetUser.getUsername());
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("targetUser", targetUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("targetUserId", user);
            model.addAttribute("targetUsername", username);
            
            return "chat/private";
        } catch (Exception e) {
            System.out.println("获取用户数据时出错: " + e.getMessage());
            return "redirect:/chat";
        }
    }

    /**
     * 会话详情页
     */
    @GetMapping("/conversation/{conversationId}")
    public String conversationDetail(@PathVariable Long conversationId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("conversationId", conversationId);
        return "chat/conversation";
    }

    /**
     * 创建私聊（从视频详情页跳转）
     */
    @GetMapping("/private")
    public String createPrivateChat(@RequestParam Long userId,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        User targetUser = userService.findById(userId).orElse(null);
        if (targetUser == null) {
            return "redirect:/chat";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("targetUser", targetUser);
        return "chat/private";
    }

    /**
     * 聊天系统测试页面
     */
    @GetMapping("/test")
    public String chatTest() {
        return "chat/test";
    }

    /**
     * 图片显示测试页面
     */
    @GetMapping("/image-test")
    public String imageTest() {
        return "image-test";
    }

    /**
     * 聊天系统功能演示页面
     */
    @GetMapping("/demo")
    public String chatDemo() {
        return "chat-demo";
    }

    /**
     * 简单聊天测试页面
     */
    @GetMapping("/simple")
    public String chatSimple(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        return "chat-simple";
    }



    /**
     * 调试认证状态
     */
    @GetMapping("/debug/auth")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugAuth(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        response.put("userDetails", userDetails != null ? userDetails.getUsername() : "null");
        response.put("authentication", auth != null ? auth.getName() : "null");
        response.put("isAuthenticated", auth != null ? auth.isAuthenticated() : false);
        response.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Chat page access test - should work without authentication");

        return ResponseEntity.ok(response);
    }



    /**
     * 聊天测试页面
     */
    @GetMapping("/success")
    public String chatSuccess() {
        return "chat-simple";
    }
}
