package org.example.controller;

import org.example.entity.User;
import org.example.service.UserService;
import org.example.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 消息页面控制器
 * 处理私信页面的路由
 */
@Controller
public class MessageController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    /**
     * 私信页面
     * 处理 /messages?user=xxx&username=xxx 的路由
     */
    @GetMapping("/messages")
    public String messagesPage(@RequestParam(required = false) Long user,
                              @RequestParam(required = false) String username,
                              Authentication authentication,
                              Model model) {
        
        // 检查用户是否已登录
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            // 获取当前用户
            User currentUser = userService.findByUsername(authentication.getName()).orElse(null);
            if (currentUser == null) {
                return "redirect:/login";
            }

            // 如果指定了目标用户，跳转到私聊页面
            if (user != null && username != null) {
                // 验证目标用户是否存在
                Optional<User> targetUserOpt = userService.findById(user);
                if (targetUserOpt.isPresent()) {
                    User targetUser = targetUserOpt.get();
                    
                    // 检查是否是给自己发消息
                    if (currentUser.getId().equals(targetUser.getId())) {
                        // 跳转到聊天首页
                        return "redirect:/chat";
                    }
                    
                    // 跳转到私聊页面
                    return "redirect:/chat?user=" + user + "&username=" + username;
                } else {
                    // 目标用户不存在，跳转到聊天首页
                    return "redirect:/chat";
                }
            }

            // 如果没有指定目标用户，跳转到聊天首页
            return "redirect:/chat";
            
        } catch (Exception e) {
            System.err.println("消息页面处理错误: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/chat";
        }
    }

    /**
     * 处理旧的私信链接格式
     * 兼容 /messages?userId=xxx 的格式
     */
    @GetMapping("/messages/private")
    public String privateMessagePage(@RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String username,
                                   Authentication authentication,
                                   Model model) {
        
        // 重定向到新的格式
        if (userId != null) {
            String redirectUrl = "/messages?user=" + userId;
            if (username != null) {
                redirectUrl += "&username=" + username;
            }
            return "redirect:" + redirectUrl;
        }
        
        return "redirect:/chat";
    }
} 