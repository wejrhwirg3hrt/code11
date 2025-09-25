package org.example.controller;

import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/account")
public class UserAccountController {

    @Autowired
    private UserService userService;

    // 显示账号注销页面
    @GetMapping("/delete")
    public String deleteAccountPage(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        Optional<User> user = userService.findActiveByUsername(authentication.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user.get());
        return "user/delete-account";
    }

    // 处理账号注销请求
    @PostMapping("/delete")
    public String deleteAccount(@RequestParam String password,
                               @RequestParam String reason,
                               @RequestParam(required = false) boolean confirm,
                               Authentication authentication,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        if (authentication == null) {
            return "redirect:/login";
        }
        
        if (!confirm) {
            redirectAttributes.addFlashAttribute("error", "请确认注销账号");
            return "redirect:/account/delete";
        }
        
        try {
            userService.selfDeleteAccount(authentication.getName(), password, reason);
            
            // 注销成功后清除会话
            session.invalidate();
            
            redirectAttributes.addFlashAttribute("success", "账号已成功注销");
            return "redirect:/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "注销失败：" + e.getMessage());
            return "redirect:/account/delete";
        }
    }

    // 账号设置页面
    @GetMapping("/settings")
    public String accountSettings(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        Optional<User> user = userService.findActiveByUsername(authentication.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user.get());
        return "user/account-settings";
    }
}