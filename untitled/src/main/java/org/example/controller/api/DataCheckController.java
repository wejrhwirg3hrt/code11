package org.example.controller.api;

import org.example.entity.User;
import org.example.entity.Video;
import org.example.entity.User;
import org.example.entity.Video;
import org.example.repository.UserRepository;
import org.example.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-check")
public class DataCheckController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Value("${app.upload.path:E:/code11/uploads}")
    private String uploadPath;

    /**
     * 检查数据持久化状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkDataStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查数据库连接
            long userCount = userRepository.count();
            long videoCount = videoRepository.count();
            
            result.put("database", Map.of(
                "users", userCount,
                "videos", videoCount,
                "status", "connected"
            ));
            
            // 检查文件存储
            File uploadDir = new File(uploadPath);
            boolean uploadDirExists = uploadDir.exists() && uploadDir.isDirectory();
            
            Map<String, Object> fileStorage = new HashMap<>();
            fileStorage.put("uploadPath", uploadPath);
            fileStorage.put("exists", uploadDirExists);
            
            if (uploadDirExists) {
                File[] subDirs = uploadDir.listFiles(File::isDirectory);
                if (subDirs != null) {
                    Map<String, Long> subDirCounts = new HashMap<>();
                    for (File subDir : subDirs) {
                        File[] files = subDir.listFiles(File::isFile);
                        subDirCounts.put(subDir.getName(), files != null ? (long) files.length : 0L);
                    }
                    fileStorage.put("subDirectories", subDirCounts);
                }
            }
            
            result.put("fileStorage", fileStorage);
            
            // 检查视频文件完整性
            List<Video> videos = videoRepository.findAll();
            Map<String, Object> videoIntegrity = new HashMap<>();
            int validVideos = 0;
            int missingFiles = 0;
            
            for (Video video : videos) {
                String filePath = video.getFilePath();
                if (filePath != null && !filePath.isEmpty()) {
                    // 移除开头的斜杠，转换为绝对路径
                    if (filePath.startsWith("/")) {
                        filePath = filePath.substring(1);
                    }
                    Path absolutePath = Paths.get(uploadPath, filePath);
                    if (Files.exists(absolutePath)) {
                        validVideos++;
                    } else {
                        missingFiles++;
                    }
                }
            }
            
            videoIntegrity.put("totalVideos", videos.size());
            videoIntegrity.put("validFiles", validVideos);
            videoIntegrity.put("missingFiles", missingFiles);
            result.put("videoIntegrity", videoIntegrity);
            
            result.put("success", true);
            result.put("message", "数据检查完成");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 修复数据不一致问题
     */
    @PostMapping("/repair")
    public ResponseEntity<Map<String, Object>> repairData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 清理无效的视频记录
            List<Video> videos = videoRepository.findAll();
            int repairedCount = 0;
            
            for (Video video : videos) {
                String filePath = video.getFilePath();
                if (filePath != null && !filePath.isEmpty()) {
                    if (filePath.startsWith("/")) {
                        filePath = filePath.substring(1);
                    }
                    Path absolutePath = Paths.get(uploadPath, filePath);
                    if (!Files.exists(absolutePath)) {
                        // 文件不存在，标记为已删除状态而不是物理删除
                        video.setStatus(Video.VideoStatus.REJECTED);
                        video.setDescription("文件已丢失，自动标记为拒绝状态");
                        videoRepository.save(video);
                        repairedCount++;
                    }
                }
            }
            
            result.put("success", true);
            result.put("repairedCount", repairedCount);
            result.put("message", "数据修复完成");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 安全的数据清理方法
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 只更新状态，不删除记录
            List<Video> videos = videoRepository.findAll();
            int updatedCount = 0;
            List<String> missingFiles = new ArrayList<>();
            
            for (Video video : videos) {
                String filePath = video.getFilePath();
                if (filePath != null && !filePath.isEmpty()) {
                    if (filePath.startsWith("/")) {
                        filePath = filePath.substring(1);
                    }
                    Path absolutePath = Paths.get(uploadPath, filePath);
                    if (!Files.exists(absolutePath)) {
                        // 只更新描述，不改变状态
                        video.setDescription("文件路径: " + filePath + " - 文件不存在");
                        videoRepository.save(video);
                        updatedCount++;
                        missingFiles.add(filePath);
                    }
                }
            }
            
            result.put("success", true);
            result.put("updatedCount", updatedCount);
            result.put("missingFiles", missingFiles);
            result.put("message", "数据清理完成，更新了 " + updatedCount + " 条记录");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取详细的数据报告
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getDataReport() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 用户统计
            List<User> users = userRepository.findAll();
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("total", users.size());
            userStats.put("active", users.stream().filter(u -> u.getEnabled() != null && u.getEnabled()).count());
            userStats.put("banned", users.stream().filter(u -> u.getBanned() != null && u.getBanned()).count());
            result.put("userStats", userStats);
            
            // 视频统计
            List<Video> videos = videoRepository.findAll();
            Map<String, Object> videoStats = new HashMap<>();
            videoStats.put("total", videos.size());
            videoStats.put("pending", videos.stream().filter(v -> "PENDING".equals(v.getStatus().name())).count());
            videoStats.put("approved", videos.stream().filter(v -> "APPROVED".equals(v.getStatus().name())).count());
            videoStats.put("rejected", videos.stream().filter(v -> "REJECTED".equals(v.getStatus().name())).count());
            result.put("videoStats", videoStats);
            
            // 文件上传统计（简化版本）
            Map<String, Object> fileStats = new HashMap<>();
            fileStats.put("total", 0);
            fileStats.put("deleted", 0);
            result.put("fileStats", fileStats);
            
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }
} 