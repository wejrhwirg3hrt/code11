package org.example.controller.api;

import org.example.entity.Achievement;
import org.example.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 调试控制器 - 用于诊断成就数据问题
 */
@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AchievementDebugController {

    @Autowired
    private AchievementRepository achievementRepository;

    /**
     * 获取成就数据统计信息
     */
    @GetMapping("/achievement-stats")
    public ResponseEntity<Map<String, Object>> getAchievementStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取所有成就
            List<Achievement> allAchievements = achievementRepository.findAll();
            
            // 统计活跃和非活跃成就
            long totalCount = allAchievements.size();
            long activeCount = allAchievements.stream().filter(Achievement::getIsActive).count();
            long inactiveCount = totalCount - activeCount;
            
            // 按分类统计
            Map<String, Long> categoryStats = allAchievements.stream()
                .collect(Collectors.groupingBy(
                    a -> a.getCategory().toString(), 
                    Collectors.counting()
                ));
            
            // 按活跃状态和分类统计
            Map<String, Map<String, Long>> categoryActiveStats = allAchievements.stream()
                .collect(Collectors.groupingBy(
                    a -> a.getCategory().toString(),
                    Collectors.groupingBy(
                        a -> a.getIsActive() ? "active" : "inactive",
                        Collectors.counting()
                    )
                ));

            // 获取前几个非活跃成就的名称
            List<String> inactiveAchievementNames = allAchievements.stream()
                .filter(a -> !a.getIsActive())
                .limit(10)
                .map(Achievement::getName)
                .collect(Collectors.toList());

            response.put("success", true);
            response.put("totalCount", totalCount);
            response.put("activeCount", activeCount);
            response.put("inactiveCount", inactiveCount);
            response.put("activePercentage", Math.round((activeCount * 100.0 / totalCount) * 100.0) / 100.0);
            response.put("categoryStats", categoryStats);
            response.put("categoryActiveStats", categoryActiveStats);
            response.put("inactiveAchievementNames", inactiveAchievementNames);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 激活所有成就
     */
    @PostMapping("/activate-all-achievements")
    public ResponseEntity<Map<String, Object>> activateAllAchievements() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Achievement> allAchievements = achievementRepository.findAll();
            long inactiveCount = allAchievements.stream().filter(a -> !a.getIsActive()).count();
            
            // 激活所有非活跃成就
            allAchievements.stream()
                .filter(a -> !a.getIsActive())
                .forEach(a -> a.setIsActive(true));
            
            achievementRepository.saveAll(allAchievements);

            response.put("success", true);
            response.put("message", "已激活所有成就");
            response.put("activatedCount", inactiveCount);
            response.put("totalCount", allAchievements.size());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
