package org.example.controller;

import org.example.service.UserService;
import org.example.service.UserLogService;
import org.example.service.AchievementService;
import org.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserLogService userLogService;

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        userLogService.logUserAction("LOGIN_PAGE_VISIT", "User visited login page");
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
            userLogService.logError("LOGIN_ERROR", "Login failed - invalid credentials", null);
        }
        return "auth/login";
    }

    @GetMapping("/auth/login")
    public String authLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        userLogService.logUserAction("LOGIN_PAGE_VISIT", "User visited login page");
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
            userLogService.logError("LOGIN_ERROR", "Login failed - invalid credentials", null);
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/auth/register")
    public String authRegister() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {

        // 验证密码确认
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "密码确认不匹配");
            return "redirect:/register";
        }

        // 检查用户名是否已存在
        if (userService.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "用户名已存在");
            return "redirect:/register";
        }

        // 检查邮箱是否已存在
        if (userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "邮箱已被注册");
            return "redirect:/register";
        }

        try {
            User newUser = userService.registerUser(username, email, password);

            // 触发注册成就检查
            try {
                achievementService.triggerAchievementCheck(newUser, "REGISTER");
                System.out.println("✅ 注册成就检查完成");
            } catch (Exception achievementError) {
                System.err.println("❌ 注册成就检查失败: " + achievementError.getMessage());
            }

            redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "注册失败：" + e.getMessage());
            return "redirect:/register";
        }
    }
}