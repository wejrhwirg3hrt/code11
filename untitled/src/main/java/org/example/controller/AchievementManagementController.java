package org.example.controller;

import org.example.entity.Achievement;
import org.example.entity.User;
import org.example.entity.UserAchievement;
import org.example.service.AchievementDetectionService;
import org.example.service.UserService;
import org.example.repository.UserAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成就管理控制器
 */
@Controller
@RequestMapping("/achievement-management")
public class AchievementManagementController {

    @Autowired
    private AchievementDetectionService achievementDetectionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    /**
     * 成就管理页面
     */
    @GetMapping
    public String achievementManagementPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);

        return "achievement-management/index";
    }

    /**
     * 检测当前用户的所有成就
     */
    @PostMapping("/api/detect")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detectUserAchievements(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(401).body(response);
        }

        User user = userOpt.get();

        try {
            List<Achievement> newAchievements = achievementDetectionService.detectAllUserAchievements(user.getId());
            
            List<Map<String, Object>> achievementList = newAchievements.stream()
                    .map(this::convertAchievementToMap)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("message", String.format("检测完成，获得了 %d 个新成就！", newAchievements.size()));
            response.put("newAchievements", achievementList);
            response.put("count", newAchievements.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检测成就时出错：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取用户成就列表
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserAchievements(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(401).body(response);
        }

        User user = userOpt.get();

        try {
            List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(user.getId());
            
            List<Map<String, Object>> achievementList = userAchievements.stream()
                    .map(this::convertUserAchievementToMap)
                    .collect(Collectors.toList());

            // 按获得时间倒序排列
            achievementList.sort((a, b) -> {
                String timeA = (String) a.get("unlockedAt");
                String timeB = (String) b.get("unlockedAt");
                return timeB.compareTo(timeA);
            });

            response.put("success", true);
            response.put("achievements", achievementList);
            response.put("totalCount", achievementList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取成就列表时出错：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 初始化默认成就
     */
    @PostMapping("/api/init")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> initializeAchievements() {
        Map<String, Object> response = new HashMap<>();

        try {
            achievementDetectionService.initializeAchievements();
            response.put("success", true);
            response.put("message", "成就系统初始化完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "初始化成就时出错：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 检测所有用户的历史成就（管理员功能）
     */
    @PostMapping("/api/detect-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detectAllUsersAchievements() {
        Map<String, Object> response = new HashMap<>();

        try {
            achievementDetectionService.detectAllUsersHistoricalAchievements();
            response.put("success", true);
            response.put("message", "所有用户的历史成就检测完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检测历史成就时出错：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取成就统计信息
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAchievementStats(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(401).body(response);
        }

        User user = userOpt.get();

        try {
            List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(user.getId());
            
            // 按稀有度分组统计
            Map<String, Long> rarityStats = userAchievements.stream()
                    .collect(Collectors.groupingBy(
                            ua -> ua.getAchievement().getRarity().name(),
                            Collectors.counting()
                    ));

            // 按分类分组统计
            Map<String, Long> categoryStats = userAchievements.stream()
                    .collect(Collectors.groupingBy(
                            ua -> ua.getAchievement().getCategory().name(),
                            Collectors.counting()
                    ));

            // 计算总经验值奖励
            int totalExpReward = userAchievements.stream()
                    .mapToInt(ua -> ua.getAchievement().getPoints())
                    .sum();

            response.put("success", true);
            response.put("totalAchievements", userAchievements.size());
            response.put("totalExpReward", totalExpReward);
            response.put("rarityStats", rarityStats);
            response.put("categoryStats", categoryStats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取成就统计时出错：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 转换成就对象为Map
     */
    private Map<String, Object> convertAchievementToMap(Achievement achievement) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", achievement.getId());
        map.put("name", achievement.getName());
        map.put("description", achievement.getDescription());
        map.put("category", achievement.getCategory().name());
        map.put("rarity", achievement.getRarity().name());
        map.put("points", achievement.getPoints());
        map.put("icon", achievement.getIcon());
        return map;
    }

    /**
     * 转换用户成就对象为Map
     */
    private Map<String, Object> convertUserAchievementToMap(UserAchievement userAchievement) {
        Map<String, Object> map = convertAchievementToMap(userAchievement.getAchievement());
        map.put("unlockedAt", userAchievement.getUnlockedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return map;
    }
}
