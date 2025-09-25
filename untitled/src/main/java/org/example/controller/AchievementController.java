package org.example.controller;

import org.example.entity.User;
import org.example.service.AchievementService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Optional;

/**
 * 成就页面控制器
 */
@Controller
public class AchievementController {

    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;

    /**
     * 显示成就页面 - 处理 /achievements 路径
     */
    @GetMapping("/achievements")
    public String showAchievementsPage(Model model, Authentication authentication) {
        return showAchievements(model, authentication);
    }

    /**
     * 显示用户成就页面 - 处理 /user-achievements 路径
     */
    @GetMapping("/user-achievements")
    public String showAchievements(Model model, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return "redirect:/auth/login";
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                return "redirect:/auth/login";
            }

            User user = userOpt.get();

            // 添加用户信息到模型
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            // 获取成就进度信息
            AchievementService.AchievementProgressInfo progressInfo = achievementService.getAchievementProgress(user);
            model.addAttribute("progressInfo", progressInfo);

            return "user-achievements";

        } catch (Exception e) {
            System.err.println("显示成就页面失败: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/stats";
        }
    }

    /**
     * 获取当前用户的成就信息 - API接口
     */
    @GetMapping("/api/achievements/current-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUserAchievements(Authentication authentication) {
        try {
            if (authentication == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }

            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 获取用户成就信息
            AchievementService.AchievementProgressInfo progressInfo = achievementService.getAchievementProgress(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("progressInfo", progressInfo);
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户成就失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
