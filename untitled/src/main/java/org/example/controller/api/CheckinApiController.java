package org.example.controller.api;

import org.example.entity.User;
import org.example.service.UserService;
import org.example.service.AchievementService;
import org.example.service.UserLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 签到系统API控制器
 */
@RestController
@RequestMapping("/api/checkin")
public class CheckinApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserLevelService userLevelService;

    /**
     * 每日签到
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> dailyCheckin(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        String username = null; // 在方法开始就声明username变量

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.ok(response);
            }

            username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            LocalDate today = LocalDate.now();
            
            // 检查今天是否已经签到
            if (user.getLastCheckinDate() != null && user.getLastCheckinDate().equals(today)) {
                response.put("success", false);
                response.put("message", "今天已经签到过了");
                response.put("alreadyCheckedIn", true);
                // 返回签到天数信息
                Integer consecutiveDays = user.getConsecutiveCheckinDays();
                Integer totalDays = user.getTotalCheckinDays();
                response.put("consecutiveDays", consecutiveDays != null ? consecutiveDays : 0);
                response.put("totalDays", totalDays != null ? totalDays : 0);
                return ResponseEntity.ok(response);
            }

            // 计算签到奖励
            int baseReward = 10; // 基础签到奖励
            int consecutiveBonus = 0;
            
            // 检查连续签到
            if (user.getLastCheckinDate() != null &&
                user.getLastCheckinDate().equals(today.minusDays(1))) {
                // 连续签到
                Integer currentConsecutiveDays = user.getConsecutiveCheckinDays();
                if (currentConsecutiveDays == null) {
                    currentConsecutiveDays = 0;
                }
                user.setConsecutiveCheckinDays(currentConsecutiveDays + 1);
                consecutiveBonus = Math.min(user.getConsecutiveCheckinDays() * 2, 50); // 最多50积分连续奖励
            } else {
                // 重新开始连续签到
                user.setConsecutiveCheckinDays(1);
            }

            int totalReward = baseReward + consecutiveBonus;

            // 更新用户签到信息
            user.setLastCheckinDate(today);
            // 处理null值，确保不会出现空指针异常
            Integer currentTotalDays = user.getTotalCheckinDays();
            if (currentTotalDays == null) {
                currentTotalDays = 0;
            }
            user.setTotalCheckinDays(currentTotalDays + 1);
            
            // 保存用户信息
            userService.save(user);

            // 添加经验值到等级系统
            userLevelService.addExperience(user, (long) totalReward);

            // 同步用户统计数据到UserLevel表
            userLevelService.syncUserStats(user);

            // 触发成就检查
            try {
                achievementService.triggerAchievementCheck(user, "DAILY_CHECKIN");
                System.out.println("✅ 签到成就检查完成: " + user.getUsername());
            } catch (Exception e) {
                System.err.println("❌ 签到成就检查失败: " + e.getMessage());
            }

            // 获取当前用户等级信息
            org.example.entity.UserLevel userLevel = userLevelService.getUserLevel(user);

            response.put("success", true);
            response.put("message", "签到成功！");
            response.put("reward", totalReward);
            // 处理null值，确保不会出现空指针异常
            Integer consecutiveDays = user.getConsecutiveCheckinDays();
            Integer totalDays = user.getTotalCheckinDays();
            response.put("consecutiveDays", consecutiveDays != null ? consecutiveDays : 0);
            response.put("totalDays", totalDays != null ? totalDays : 0);
            response.put("currentPoints", userLevel.getExperiencePoints());
            response.put("currentLevel", userLevel.getLevel());
            
            System.out.println("🎯 用户签到成功: " + username + ", 获得积分: " + totalReward);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 签到失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "签到失败: " + e.getMessage());

            // 即使签到失败，也要返回当前的签到状态信息
            try {
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    LocalDate today = LocalDate.now();

                    // 检查今天是否已经签到过
                    boolean alreadyCheckedIn = user.getLastCheckinDate() != null &&
                                             user.getLastCheckinDate().equals(today);
                    response.put("alreadyCheckedIn", alreadyCheckedIn);

                    // 返回当前签到信息
                    Integer consecutiveDays = user.getConsecutiveCheckinDays();
                    Integer totalDays = user.getTotalCheckinDays();
                    response.put("consecutiveDays", consecutiveDays != null ? consecutiveDays : 0);
                    response.put("totalDays", totalDays != null ? totalDays : 0);

                    // 如果已经签到过，获取用户等级信息
                    if (alreadyCheckedIn) {
                        org.example.entity.UserLevel userLevel = userLevelService.getUserLevel(user);
                        response.put("currentPoints", userLevel.getExperiencePoints());
                        response.put("currentLevel", userLevel.getLevel());
                    }
                }
            } catch (Exception statusException) {
                System.err.println("❌ 获取签到状态失败: " + statusException.getMessage());
                // 如果获取状态也失败，设置默认值
                response.put("alreadyCheckedIn", false);
                response.put("consecutiveDays", 0);
                response.put("totalDays", 0);
            }

            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取签到状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCheckinStatus(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.ok(response);
            }

            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            LocalDate today = LocalDate.now();
            
            boolean hasCheckedInToday = user.getLastCheckinDate() != null && 
                                      user.getLastCheckinDate().equals(today);

            response.put("success", true);
            response.put("hasCheckedInToday", hasCheckedInToday);
            // 处理null值，确保不会出现空指针异常
            Integer consecutiveDays = user.getConsecutiveCheckinDays();
            Integer totalDays = user.getTotalCheckinDays();
            response.put("consecutiveDays", consecutiveDays != null ? consecutiveDays : 0);
            response.put("totalDays", totalDays != null ? totalDays : 0);
            response.put("lastCheckinDate", user.getLastCheckinDate());

            // 获取用户等级信息
            org.example.entity.UserLevel userLevel = userLevelService.getUserLevel(user);
            response.put("currentPoints", userLevel.getExperiencePoints());
            response.put("currentLevel", userLevel.getLevel());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 获取签到状态失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "获取签到状态失败");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取每日任务状态
     */
    @GetMapping("/daily-tasks")
    public ResponseEntity<Map<String, Object>> getDailyTasks(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.ok(response);
            }

            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);

            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();

            // 获取用户等级信息
            org.example.entity.UserLevel userLevel = userLevelService.getUserLevel(user);

            // 模拟每日任务数据
            response.put("success", true);
            response.put("tasks", new Object[]{
                new HashMap<String, Object>() {{
                    put("id", "daily_checkin");
                    put("name", "每日签到");
                    put("description", "完成每日签到获得经验值");
                    put("progress", user.getLastCheckinDate() != null &&
                        user.getLastCheckinDate().equals(java.time.LocalDate.now()) ? 1 : 0);
                    put("target", 1);
                    put("reward", 10);
                    put("completed", user.getLastCheckinDate() != null &&
                        user.getLastCheckinDate().equals(java.time.LocalDate.now()));
                }}
            });

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 获取每日任务失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "获取每日任务失败");
            return ResponseEntity.ok(response);
        }
    }
}
