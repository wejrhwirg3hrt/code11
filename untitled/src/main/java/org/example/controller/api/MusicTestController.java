package org.example.controller.api;

import org.example.entity.Music;
import org.example.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/music-test")
@CrossOrigin(originPatterns = "*")
public class MusicTestController {

    @Autowired
    private MusicRepository musicRepository;

    /**
     * 测试数据库连接
     */
    @GetMapping("/db-connection")
    public ResponseEntity<Map<String, Object>> testDbConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 尝试查询数据库
            long count = musicRepository.count();
            response.put("success", true);
            response.put("message", "数据库连接正常");
            response.put("totalMusicCount", count);
            response.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "数据库连接失败: " + e.getMessage());
            response.put("error", e.toString());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试音乐保存
     */
    @PostMapping("/test-save")
    public ResponseEntity<Map<String, Object>> testMusicSave() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 创建测试音乐
            Music testMusic = new Music();
            testMusic.setTitle("测试音乐_" + System.currentTimeMillis());
            testMusic.setArtist("测试艺术家");
            testMusic.setAlbum("测试专辑");
            testMusic.setFileName("test_music.mp3");
            testMusic.setFilePath("/uploads/music/test_music.mp3");
            testMusic.setUserId(1L); // 假设用户ID为1
            testMusic.setUploadTime(LocalDateTime.now());
            testMusic.setPlayCount(0L);
            testMusic.setIsPublic(true);
            
            System.out.println("🧪 测试保存音乐:");
            System.out.println("  - 标题: " + testMusic.getTitle());
            System.out.println("  - 艺术家: " + testMusic.getArtist());
            System.out.println("  - 文件路径: " + testMusic.getFilePath());
            
            // 保存到数据库
            Music savedMusic = musicRepository.save(testMusic);
            
            response.put("success", true);
            response.put("message", "测试音乐保存成功");
            response.put("savedMusicId", savedMusic.getId());
            response.put("savedMusic", Map.of(
                "id", savedMusic.getId(),
                "title", savedMusic.getTitle(),
                "artist", savedMusic.getArtist(),
                "filePath", savedMusic.getFilePath(),
                "uploadTime", savedMusic.getUploadTime()
            ));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "测试音乐保存失败: " + e.getMessage());
            response.put("error", e.toString());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有音乐记录
     */
    @GetMapping("/all-music")
    public ResponseEntity<Map<String, Object>> getAllMusic() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Music> allMusic = musicRepository.findAll();
            
            response.put("success", true);
            response.put("totalCount", allMusic.size());
            response.put("musicList", allMusic.stream()
                .map(m -> {
                    Map<String, Object> musicMap = new HashMap<>();
                    musicMap.put("id", m.getId());
                    musicMap.put("title", m.getTitle());
                    musicMap.put("artist", m.getArtist());
                    musicMap.put("album", m.getAlbum());
                    musicMap.put("fileName", m.getFileName());
                    musicMap.put("filePath", m.getFilePath());
                    musicMap.put("coverPath", m.getCoverPath());
                    musicMap.put("userId", m.getUserId());
                    musicMap.put("uploadTime", m.getUploadTime());
                    musicMap.put("playCount", m.getPlayCount());
                    musicMap.put("isPublic", m.getIsPublic());
                    return musicMap;
                })
                .toList());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取音乐列表失败: " + e.getMessage());
            response.put("error", e.toString());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 清理测试数据
     */
    @DeleteMapping("/cleanup-test")
    public ResponseEntity<Map<String, Object>> cleanupTestData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 删除测试音乐（标题包含"测试音乐"的记录）
            List<Music> testMusic = musicRepository.findAll().stream()
                .filter(m -> m.getTitle() != null && m.getTitle().contains("测试音乐"))
                .toList();
            
            musicRepository.deleteAll(testMusic);
            
            response.put("success", true);
            response.put("message", "测试数据清理成功");
            response.put("deletedCount", testMusic.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "测试数据清理失败: " + e.getMessage());
            response.put("error", e.toString());
        }
        
        return ResponseEntity.ok(response);
    }
} 