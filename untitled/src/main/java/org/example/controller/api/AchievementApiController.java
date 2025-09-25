package org.example.controller.api;

import org.example.entity.Achievement;
import org.example.entity.User;
import org.example.entity.UserAchievement;
import org.example.repository.AchievementRepository;
import org.example.service.AchievementService;
import org.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成就API控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AchievementApiController {

    private static final Logger logger = LoggerFactory.getLogger(AchievementApiController.class);

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserService userService;

    /**
     * 简单测试端点
     */
    @GetMapping("/simple-test/hello")
    public ResponseEntity<Map<String, Object>> simpleTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Hello from Achievement API!");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取成就数据端点
     */
    @GetMapping("/achievement-data")
    public ResponseEntity<Map<String, Object>> getAchievementData() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Achievement> achievements = achievementService.getAllAchievements();
            response.put("success", true);
            response.put("totalCount", achievements.size());
            response.put("message", "成就API工作正常");
            response.put("achievements", achievements);

            // 添加调试信息
            List<Achievement> allIncludingInactive = achievementService.getAllAchievementsIncludingInactive();
            long activeCount = allIncludingInactive.stream().filter(a -> a.getIsActive()).count();
            long inactiveCount = allIncludingInactive.stream().filter(a -> !a.getIsActive()).count();

            response.put("debug", Map.of(
                "repositoryTotalCount", achievementRepository.count(),
                "serviceActiveCount", achievements.size(),
                "allIncludingInactiveCount", allIncludingInactive.size(),
                "activeCount", activeCount,
                "inactiveCount", inactiveCount,
                "firstFewActive", achievements.stream().limit(3).map(a -> a.getName()).toList(),
                "firstFewInactive", allIncludingInactive.stream().filter(a -> !a.getIsActive()).limit(3).map(a -> a.getName()).toList()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取成就失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有成就数据（无需认证，用于测试）
     */
    @GetMapping("/achievement-data/public")
    public ResponseEntity<Map<String, Object>> getPublicAchievements() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Achievement> allAchievements = achievementService.getAllAchievements();

            List<Map<String, Object>> achievementList = new ArrayList<>();
            for (Achievement achievement : allAchievements) {
                Map<String, Object> achievementData = new HashMap<>();
                achievementData.put("id", achievement.getId());
                achievementData.put("name", achievement.getName());
                achievementData.put("description", achievement.getDescription());
                achievementData.put("icon", achievement.getIcon());
                achievementData.put("points", achievement.getPoints());
                achievementData.put("category", achievement.getCategory().name());
                achievementData.put("categoryDescription", achievement.getCategory().getDescription());
                achievementData.put("rarity", achievement.getRarity().name());
                achievementData.put("rarityDescription", achievement.getRarity().getDescription());
                achievementData.put("rarityColor", achievement.getRarity().getColor());
                achievementData.put("conditionType", achievement.getConditionType());
                achievementData.put("conditionValue", achievement.getConditionValue());
                achievementData.put("unlocked", false);
                achievementData.put("unlockedAt", null);
                achievementList.add(achievementData);
            }

            response.put("success", true);
            response.put("data", achievementList);
            response.put("totalCount", achievementList.size());

        } catch (Exception e) {
            System.err.println("获取公开成就失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取成就失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }



    /**
     * 获取用户所有成就
     */
    @GetMapping("/achievement-data/all")
    public ResponseEntity<Map<String, Object>> getAllUserAchievements(
            Authentication authentication,
            @RequestParam(defaultValue = "all") String filter) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || authentication.getName() == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
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

            // 获取所有成就
            List<Achievement> allAchievements = achievementService.getAllAchievements();

            // 获取用户已解锁的成就
            List<UserAchievement> userAchievements = achievementService.getUserAchievements(user);
            Map<Long, UserAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), ua -> ua));

            // 构建成就列表
            List<Map<String, Object>> achievementList = new ArrayList<>();
            for (Achievement achievement : allAchievements) {
                UserAchievement userAchievement = userAchievementMap.get(achievement.getId());
                boolean unlocked = userAchievement != null;

                // 根据筛选条件过滤
                if ("unlocked".equals(filter) && !unlocked) {
                    continue;
                }
                if ("locked".equals(filter) && unlocked) {
                    continue;
                }

                Map<String, Object> achievementData = convertAchievementToMap(achievement, userAchievement);
                achievementList.add(achievementData);
            }

            response.put("success", true);
            response.put("data", achievementList);

        } catch (Exception e) {
            logger.error("获取所有成就失败", e);
            response.put("success", false);
            response.put("message", "获取成就失败");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 将UserAchievement列表转换为Map列表
     */
    private List<Map<String, Object>> convertUserAchievementsToMap(List<UserAchievement> userAchievements) {
        return userAchievements.stream()
            .map(this::convertUserAchievementToMap)
            .collect(Collectors.toList());
    }

    /**
     * 将UserAchievement转换为Map
     */
    private Map<String, Object> convertUserAchievementToMap(UserAchievement userAchievement) {
        Map<String, Object> map = new HashMap<>();
        Achievement achievement = userAchievement.getAchievement();

        map.put("id", achievement.getId());
        map.put("name", achievement.getName());
        map.put("description", achievement.getDescription());
        map.put("icon", achievement.getIcon() != null ? achievement.getIcon() : "fa-trophy");
        map.put("category", achievement.getCategory().getDescription());
        map.put("rarity", achievement.getRarity().getDescription());
        map.put("color", achievement.getRarity().getColor());
        map.put("points", achievement.getPoints());
        map.put("unlocked", true);
        map.put("achievedAt", userAchievement.getUnlockedAt());
        map.put("progress", userAchievement.getProgress());
        map.put("isDisplayed", userAchievement.getIsDisplayed());

        return map;
    }

    /**
     * 将Achievement转换为Map（包含解锁状态）
     */
    private Map<String, Object> convertAchievementToMap(Achievement achievement, UserAchievement userAchievement) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", achievement.getId());
        map.put("name", achievement.getName());
        map.put("description", achievement.getDescription());
        map.put("icon", achievement.getIcon() != null ? achievement.getIcon() : "fa-trophy");
        map.put("category", achievement.getCategory().getDescription());
        map.put("rarity", achievement.getRarity().getDescription());
        map.put("color", achievement.getRarity().getColor());
        map.put("points", achievement.getPoints());
        map.put("conditionType", achievement.getConditionType());
        map.put("conditionValue", achievement.getConditionValue());

        if (userAchievement != null) {
            map.put("unlocked", true);
            map.put("achievedAt", userAchievement.getUnlockedAt());
            map.put("progress", userAchievement.getProgress());
            map.put("isDisplayed", userAchievement.getIsDisplayed());
        } else {
            map.put("unlocked", false);
            map.put("progress", 0.0);
            map.put("isDisplayed", false);
        }

        return map;
    }

    /**
     * 获取用户成就进度详情
     */
    @GetMapping("/achievement-progress")
    public ResponseEntity<Map<String, Object>> getUserAchievementProgress(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || authentication.getName() == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.ok(response);
            }

            // 获取当前用户
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            Map<String, Object> progressData = achievementService.getUserAchievementProgress(user);

            response.put("success", true);
            response.put("data", progressData);

        } catch (Exception e) {
            System.err.println("获取成就进度失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取成就进度失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取成就排行榜
     */
    @GetMapping("/achievement-leaderboard")
    public ResponseEntity<Map<String, Object>> getAchievementLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> response = achievementService.getAchievementLeaderboard(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取分类成就排行榜
     */
    @GetMapping("/achievement-leaderboard/category/{category}")
    public ResponseEntity<Map<String, Object>> getCategoryLeaderboard(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> response = achievementService.getCategoryLeaderboard(category, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取最近解锁成就排行
     */
    @GetMapping("/achievement-leaderboard/recent")
    public ResponseEntity<Map<String, Object>> getRecentAchievementsLeaderboard(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "50") int limit) {

        Map<String, Object> response = achievementService.getRecentAchievementsLeaderboard(days, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户最近成就
     */
    @GetMapping("/recent-achievements")
    public ResponseEntity<Map<String, Object>> getUserRecentAchievements(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || authentication.getName() == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.ok(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            List<UserAchievement> recentAchievements = achievementService.getRecentUserAchievements(user, limit);

            List<Map<String, Object>> achievementList = new ArrayList<>();
            for (UserAchievement ua : recentAchievements) {
                Map<String, Object> achievementData = new HashMap<>();
                achievementData.put("id", ua.getAchievement().getId());
                achievementData.put("name", ua.getAchievement().getName());
                achievementData.put("description", ua.getAchievement().getDescription());
                achievementData.put("icon", ua.getAchievement().getIcon());
                achievementData.put("points", ua.getAchievement().getPoints());
                achievementData.put("rarity", ua.getAchievement().getRarity());
                achievementData.put("category", ua.getAchievement().getCategory());
                achievementData.put("unlockedAt", ua.getUnlockedAt());
                achievementData.put("unlocked", true);
                achievementList.add(achievementData);
            }

            response.put("success", true);
            response.put("recentAchievements", achievementList);
            response.put("count", achievementList.size());

        } catch (Exception e) {
            System.err.println("获取用户最近成就失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "获取用户最近成就失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户排名
     */
    @GetMapping("/achievement-ranking")
    public ResponseEntity<Map<String, Object>> getUserRanking(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || authentication.getName() == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.ok(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            Map<String, Object> ranking = achievementService.getUserRanking(userOpt.get());
            return ResponseEntity.ok(ranking);

        } catch (Exception e) {
            System.err.println("获取用户排名失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "获取用户排名失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }



}
