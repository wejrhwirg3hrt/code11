package org.example.controller.api;

import org.example.entity.Category;
import org.example.entity.Video;
import org.example.repository.CategoryRepository;
import org.example.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 分类API控制器
 * 提供动态分类数据
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(originPatterns = "*")
public class CategoryApiController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VideoRepository videoRepository;

    /**
     * 获取热门分类
     * 根据视频数量和观看量动态排序
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularCategories(
            @RequestParam(defaultValue = "4") int limit) {
        try {
            // 获取所有活跃分类
            List<Category> allCategories = categoryRepository.findByIsActiveTrueOrderBySortOrder();
            
            // 计算每个分类的热度
            List<Map<String, Object>> categoryStats = allCategories.stream()
                    .map(category -> {
                        Map<String, Object> stats = new HashMap<>();
                        stats.put("id", category.getId());
                        stats.put("name", category.getName());
                        stats.put("description", category.getDescription());
                        stats.put("icon", category.getIcon() != null ? category.getIcon() : "fas fa-video");
                        stats.put("color", category.getColor() != null ? category.getColor() : "#007bff");
                        
                        // 计算该分类下的视频数量
                        long videoCount = videoRepository.countByCategoryIdAndStatus(
                                category.getId(), Video.VideoStatus.APPROVED);
                        stats.put("videoCount", videoCount);
                        
                        // 计算该分类下的总观看量
                        List<Video> categoryVideos = videoRepository.findByCategoryIdAndStatus(
                                category.getId(), Video.VideoStatus.APPROVED);
                        long totalViews = categoryVideos.stream()
                                .mapToLong(video -> video.getViews() != null ? video.getViews() : 0)
                                .sum();
                        stats.put("totalViews", totalViews);
                        
                        // 计算热度分数 (视频数量 * 10 + 总观看量 / 100)
                        double hotScore = videoCount * 10 + totalViews / 100.0;
                        stats.put("hotScore", hotScore);
                        
                        return stats;
                    })
                    .filter(stats -> (Long) stats.get("videoCount") > 0) // 只显示有视频的分类
                    .sorted((a, b) -> Double.compare((Double) b.get("hotScore"), (Double) a.get("hotScore")))
                    .limit(limit)
                    .collect(Collectors.toList());
            
            // 如果没有足够的分类，添加默认分类
            if (categoryStats.size() < limit) {
                addDefaultCategories(categoryStats, limit);
            }
            
            return ResponseEntity.ok(categoryStats);
        } catch (Exception e) {
            // 返回默认分类
            List<Map<String, Object>> defaultCategories = getDefaultCategories(limit);
            return ResponseEntity.ok(defaultCategories);
        }
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrder();
            
            List<Map<String, Object>> categoryList = categories.stream()
                    .map(category -> {
                        Map<String, Object> categoryMap = new HashMap<>();
                        categoryMap.put("id", category.getId());
                        categoryMap.put("name", category.getName());
                        categoryMap.put("description", category.getDescription());
                        categoryMap.put("icon", category.getIcon() != null ? category.getIcon() : "fas fa-video");
                        categoryMap.put("color", category.getColor() != null ? category.getColor() : "#007bff");
                        
                        // 计算视频数量
                        long videoCount = videoRepository.countByCategoryIdAndStatus(
                                category.getId(), Video.VideoStatus.APPROVED);
                        categoryMap.put("videoCount", videoCount);
                        
                        return categoryMap;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(categoryList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(List.of(Map.of("error", "获取分类失败: " + e.getMessage())));
        }
    }

    /**
     * 添加默认分类
     */
    private void addDefaultCategories(List<Map<String, Object>> categoryStats, int limit) {
        String[][] defaultCats = {
                {"科技", "fas fa-laptop-code", "#28a745", "最新科技资讯和教程"},
                {"娱乐", "fas fa-smile", "#ffc107", "搞笑娱乐内容"},
                {"教育", "fas fa-graduation-cap", "#17a2b8", "学习教育视频"},
                {"音乐", "fas fa-music", "#e83e8c", "音乐MV和翻唱"},
                {"游戏", "fas fa-gamepad", "#6f42c1", "游戏实况和攻略"},
                {"生活", "fas fa-heart", "#fd7e14", "生活分享和vlog"}
        };
        
        int currentSize = categoryStats.size();
        for (int i = 0; i < Math.min(defaultCats.length, limit - currentSize); i++) {
            Map<String, Object> defaultCat = new HashMap<>();
            defaultCat.put("id", -(i + 1)); // 负数ID表示默认分类
            defaultCat.put("name", defaultCats[i][0]);
            defaultCat.put("icon", defaultCats[i][1]);
            defaultCat.put("color", defaultCats[i][2]);
            defaultCat.put("description", defaultCats[i][3]);
            defaultCat.put("videoCount", 0L);
            defaultCat.put("totalViews", 0L);
            defaultCat.put("hotScore", 0.0);
            categoryStats.add(defaultCat);
        }
    }

    /**
     * 获取默认分类列表
     */
    private List<Map<String, Object>> getDefaultCategories(int limit) {
        List<Map<String, Object>> defaultCategories = new ArrayList<>();
        addDefaultCategories(defaultCategories, limit);
        return defaultCategories;
    }
}
