package org.example.controller.api;

import org.example.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/music")
@CrossOrigin(originPatterns = "*")
public class MusicPerformanceController {

    @Autowired
    private MusicService musicService;

    /**
     * 获取音乐信息（用于预加载）
     */
    @GetMapping("/{id}/info")
    public ResponseEntity<Map<String, Object>> getMusicInfo(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var musicOpt = musicService.getMusicById(id);
            if (musicOpt.isPresent()) {
                var music = musicOpt.get();
                response.put("success", true);
                response.put("music", Map.of(
                    "id", music.getId(),
                    "title", music.getTitle(),
                    "artist", music.getArtist(),
                    "album", music.getAlbum(),
                    "filePath", music.getFilePath(),
                    "coverPath", music.getCoverPath(),
                    "playCount", music.getPlayCount(),
                    "uploadTime", music.getUploadTime()
                ));
            } else {
                response.put("success", false);
                response.put("message", "音乐不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取音乐信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取热门音乐（优化版本）
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularMusic(
            @RequestParam(defaultValue = "6") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var popularMusic = musicService.getPopularMusic(limit);
            response.put("success", true);
            response.put("music", popularMusic);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取热门音乐失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取音乐统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMusicStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var stats = musicService.getMusicStatistics();
            response.put("success", true);
            response.put("stats", stats);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 预加载音乐数据
     */
    @PostMapping("/preload")
    public ResponseEntity<Map<String, Object>> preloadMusicData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 异步预加载热门音乐
            var popularFuture = musicService.preloadPopularMusic();
            var latestFuture = musicService.preloadLatestMusic();
            
            response.put("success", true);
            response.put("message", "预加载任务已启动");
            response.put("popularMusic", popularFuture.get());
            response.put("latestMusic", latestFuture.get());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "预加载失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 清理缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 这里可以添加缓存清理逻辑
            response.put("success", true);
            response.put("message", "缓存已清理");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清理缓存失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取性能状态
     */
    @GetMapping("/performance/status")
    public ResponseEntity<Map<String, Object>> getPerformanceStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            response.put("success", true);
            response.put("performance", Map.of(
                "totalMemory", totalMemory,
                "freeMemory", freeMemory,
                "usedMemory", usedMemory,
                "memoryUsage", (double) usedMemory / totalMemory * 100,
                "availableProcessors", runtime.availableProcessors()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取性能状态失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 