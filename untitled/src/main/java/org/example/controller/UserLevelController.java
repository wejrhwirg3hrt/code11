package org.example.controller;

import org.example.entity.User;
import org.example.service.UserLevelService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserLevelController {

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private UserService userService;

    @GetMapping("/level")
    public String userLevel(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);

            if (!userOpt.isPresent()) {
                model.addAttribute("error", "用户不存在");
                return "error";
            }

            User user = userOpt.get();

            // 获取用户等级信息
            model.addAttribute("user", user);
            model.addAttribute("userLevel", userLevelService.getUserLevel(user));
            model.addAttribute("levelProgress", userLevelService.getLevelProgressInfo(user));
            model.addAttribute("nextLevelRequirements", userLevelService.getNextLevelRequirements(user));
            
            return "user-level";
            
        } catch (Exception e) {
            model.addAttribute("error", "获取等级信息失败: " + e.getMessage());
            return "error";
        }
    }


}
