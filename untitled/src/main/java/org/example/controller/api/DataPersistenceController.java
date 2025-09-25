package org.example.controller.api;

import org.example.entity.Music;
import org.example.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class DataPersistenceController {

    @Autowired
    private MusicRepository musicRepository;

    @GetMapping("/music-persistence-status")
    public ResponseEntity<Map<String, Object>> checkMusicPersistenceStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Music> dbMusicList = musicRepository.findAll();
            String uploadDir = "uploads/music";
            Path uploadPath = Paths.get(uploadDir);
            List<File> fileSystemMusic = new ArrayList<>();
            
            if (Files.exists(uploadPath)) {
                File[] files = uploadPath.toFile().listFiles((dir, name) -> 
                    name.toLowerCase().endsWith(".mp3") || 
                    name.toLowerCase().endsWith(".wav") || 
                    name.toLowerCase().endsWith(".flac") ||
                    name.toLowerCase().endsWith(".m4a"));
                
                if (files != null) {
                    fileSystemMusic = Arrays.asList(files);
                }
            }

            response.put("success", true);
            response.put("databaseRecords", dbMusicList.size());
            response.put("fileSystemFiles", fileSystemMusic.size());
            response.put("databaseMusic", dbMusicList.stream()
                .map(m -> Map.of(
                    "id", m.getId(),
                    "title", m.getTitle(),
                    "artist", m.getArtist(),
                    "filePath", m.getFilePath(),
                    "uploadTime", m.getUploadTime()
                ))
                .collect(Collectors.toList()));

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-music-upload")
    public ResponseEntity<Map<String, Object>> testMusicUpload() {
        Map<String, Object> response = new HashMap<>();

        try {
            Music testMusic = new Music();
            testMusic.setTitle("测试音乐");
            testMusic.setArtist("测试艺术家");
            testMusic.setFilePath("/uploads/music/test.mp3");
            testMusic.setUploadTime(LocalDateTime.now());
            
            Music savedMusic = musicRepository.save(testMusic);
            
            response.put("success", true);
            response.put("message", "测试音乐上传成功");
            response.put("savedMusicId", savedMusic.getId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
} 