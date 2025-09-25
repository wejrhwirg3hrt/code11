package org.example.controller;

import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class AboutController {

    @Autowired
    private UserService userService;

    @GetMapping("/about")
    public String about(Model model, Authentication authentication) {
        // 添加用户信息
        if (authentication != null) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }
        model.addAttribute("isAuthenticated", authentication != null);
        
        return "about/company";
    }

    @GetMapping("/team")
    public String team(Model model, Authentication authentication) {
        // 添加用户信息
        if (authentication != null) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }
        model.addAttribute("isAuthenticated", authentication != null);
        
        return "about/team";
    }

    @GetMapping("/careers")
    public String careers(Model model, Authentication authentication) {
        // 添加用户信息
        if (authentication != null) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }
        model.addAttribute("isAuthenticated", authentication != null);
        
        return "about/careers";
    }

    @GetMapping("/news")
    public String news(Model model, Authentication authentication) {
        // 添加用户信息
        if (authentication != null) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }
        model.addAttribute("isAuthenticated", authentication != null);
        
        return "about/news";
    }
} 