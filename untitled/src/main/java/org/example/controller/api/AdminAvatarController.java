package org.example.controller.api;

import org.example.service.UserLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminAvatarController {

    @Autowired
    private UserLogService userLogService;

    /**
     * 确保默认头像文件存在
     */
    @PostMapping("/ensure-default-avatar")
    public ResponseEntity<Map<String, Object>> ensureDefaultAvatar() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 调用UserLogService的方法确保默认头像存在
            userLogService.ensureDefaultAvatarExists();
            
            response.put("success", true);
            response.put("message", "默认头像文件已确保存在");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "确保默认头像失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 列出所有头像文件
     */
    @GetMapping("/list-avatar-files")
    public ResponseEntity<Map<String, Object>> listAvatarFiles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path avatarsDir = Paths.get("uploads/avatars");
            List<Map<String, Object>> files = new ArrayList<>();
            
            if (Files.exists(avatarsDir)) {
                Files.walk(avatarsDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("name", file.getFileName().toString());
                            fileInfo.put("size", Files.size(file));
                            fileInfo.put("path", file.toString());
                            files.add(fileInfo);
                        } catch (IOException e) {
                            // 忽略无法读取的文件
                        }
                    });
            }
            
            response.put("success", true);
            response.put("files", files);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取文件列表失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试头像保护功能
     */
    @PostMapping("/test-avatar-protection")
    public ResponseEntity<Map<String, Object>> testAvatarProtection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path avatarsDir = Paths.get("uploads/avatars");
            List<String> allFiles = new ArrayList<>();
            List<String> protectedFiles = new ArrayList<>();
            List<String> deletableFiles = new ArrayList<>();
            
            if (Files.exists(avatarsDir)) {
                Files.walk(avatarsDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        allFiles.add(fileName);
                        
                        if (userLogService.isDefaultAvatarFile(file)) {
                            protectedFiles.add(fileName);
                        } else {
                            deletableFiles.add(fileName);
                        }
                    });
            }
            
            response.put("success", true);
            response.put("totalFiles", allFiles.size());
            response.put("protectedFiles", protectedFiles.size());
            response.put("deletableFiles", deletableFiles.size());
            response.put("protectedFileNames", protectedFiles);
            response.put("deletableFileNames", deletableFiles);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "测试保护功能失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 模拟头像清理操作（不实际删除文件）
     */
    @PostMapping("/simulate-avatar-cleanup")
    public ResponseEntity<Map<String, Object>> simulateAvatarCleanup() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path avatarsDir = Paths.get("uploads/avatars");
            List<String> checkedFiles = new ArrayList<>();
            List<String> deletedFiles = new ArrayList<>();
            List<String> protectedFiles = new ArrayList<>();
            
            if (Files.exists(avatarsDir)) {
                Files.walk(avatarsDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        checkedFiles.add(fileName);
                        
                        if (userLogService.isDefaultAvatarFile(file)) {
                            protectedFiles.add(fileName);
                        } else {
                            deletedFiles.add(fileName);
                        }
                    });
            }
            
            response.put("success", true);
            response.put("checkedFiles", checkedFiles.size());
            response.put("deletedFiles", deletedFiles.size());
            response.put("protectedFiles", protectedFiles.size());
            response.put("deletedFileNames", deletedFiles);
            response.put("protectedFileNames", protectedFiles);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "模拟清理失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 执行头像清理操作（实际删除非默认头像）
     */
    @PostMapping("/cleanup-avatars")
    public ResponseEntity<Map<String, Object>> cleanupAvatars() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path avatarsDir = Paths.get("uploads/avatars");
            List<String> deletedFiles = new ArrayList<>();
            List<String> protectedFiles = new ArrayList<>();
            
            if (Files.exists(avatarsDir)) {
                Files.walk(avatarsDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        
                        if (userLogService.isDefaultAvatarFile(file)) {
                            protectedFiles.add(fileName);
                        } else {
                            try {
                                Files.delete(file);
                                deletedFiles.add(fileName);
                            } catch (IOException e) {
                                // 记录删除失败的文件
                                System.err.println("删除文件失败: " + file + " - " + e.getMessage());
                            }
                        }
                    });
            }
            
            response.put("success", true);
            response.put("deletedCount", deletedFiles.size());
            response.put("protectedCount", protectedFiles.size());
            response.put("deletedFiles", deletedFiles);
            response.put("protectedFiles", protectedFiles);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清理操作失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 