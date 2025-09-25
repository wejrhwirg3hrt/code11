package org.example.controller.api;

import org.example.entity.Music;
import org.example.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/music-fix")
@CrossOrigin(originPatterns = "*")
public class MusicFixController {

    @Autowired
    private MusicRepository musicRepository;

    /**
     * 检查数据库中的音乐路径问题
     */
    @GetMapping("/check-paths")
    public ResponseEntity<Map<String, Object>> checkMusicPaths() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Music> allMusic = musicRepository.findAll();
            List<Music> wrongPathMusic = allMusic.stream()
                .filter(music -> music.getFilePath() != null && music.getFilePath().startsWith("/files/"))
                .toList();
            
            response.put("success", true);
            response.put("totalMusic", allMusic.size());
            response.put("wrongPathMusic", wrongPathMusic.size());
            response.put("wrongPathList", wrongPathMusic.stream()
                .map(music -> Map.of(
                    "id", music.getId(),
                    "title", music.getTitle(),
                    "oldPath", music.getFilePath(),
                    "newPath", music.getFilePath().replace("/files/", "/uploads/")
                ))
                .toList());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 修复数据库中的音乐路径
     */
    @PostMapping("/fix-paths")
    public ResponseEntity<Map<String, Object>> fixMusicPaths() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Music> allMusic = musicRepository.findAll();
            int fixedCount = 0;
            
            for (Music music : allMusic) {
                if (music.getFilePath() != null && music.getFilePath().startsWith("/files/")) {
                    String newPath = music.getFilePath().replace("/files/", "/uploads/");
                    music.setFilePath(newPath);
                    musicRepository.save(music);
                    fixedCount++;
                    
                    System.out.println("🔧 修复音乐路径:");
                    System.out.println("  - ID: " + music.getId());
                    System.out.println("  - 标题: " + music.getTitle());
                    System.out.println("  - 旧路径: " + music.getFilePath().replace("/uploads/", "/files/"));
                    System.out.println("  - 新路径: " + newPath);
                }
            }
            
            response.put("success", true);
            response.put("message", "路径修复完成");
            response.put("fixedCount", fixedCount);
            response.put("totalMusic", allMusic.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有音乐记录（用于调试）
     */
    @GetMapping("/all-music")
    public ResponseEntity<Map<String, Object>> getAllMusic() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Music> allMusic = musicRepository.findAll();
            
            response.put("success", true);
            response.put("totalCount", allMusic.size());
            response.put("musicList", allMusic.stream()
                .map(music -> Map.of(
                    "id", music.getId(),
                    "title", music.getTitle(),
                    "artist", music.getArtist(),
                    "filePath", music.getFilePath(),
                    "uploadTime", music.getUploadTime(),
                    "playCount", music.getPlayCount()
                ))
                .toList());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }
} 