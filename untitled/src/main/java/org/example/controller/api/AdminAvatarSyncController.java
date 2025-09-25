package org.example.controller.api;

import org.example.service.AvatarSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/avatar-sync")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminAvatarSyncController {

    @Autowired
    private AvatarSyncService avatarSyncService;

    /**
     * 同步所有用户的头像到默认头像
     */
    @PostMapping("/sync-all")
    public ResponseEntity<Map<String, Object>> syncAllAvatars() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            avatarSyncService.syncAllAvatarsToDefault();
            
            response.put("success", true);
            response.put("message", "所有用户头像已同步到默认头像");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "同步失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 同步指定用户的头像
     */
    @PostMapping("/sync-user/{userId}")
    public ResponseEntity<Map<String, Object>> syncUserAvatar(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = avatarSyncService.syncUserAvatar(userId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "用户头像已同步到默认头像");
            } else {
                response.put("success", true);
                response.put("message", "用户头像无需同步");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "同步失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 批量修复头像路径
     */
    @PostMapping("/batch-fix")
    public ResponseEntity<Map<String, Object>> batchFixAvatarPaths(
            @RequestParam(defaultValue = "100") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int fixedCount = avatarSyncService.batchFixAvatarPaths(limit);
            
            response.put("success", true);
            response.put("message", "批量修复完成");
            response.put("fixedCount", fixedCount);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量修复失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取头像统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAvatarStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String statistics = avatarSyncService.getAvatarStatistics();
            
            response.put("success", true);
            response.put("statistics", statistics);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 修复指定的头像路径
     */
    @PostMapping("/fix-path")
    public ResponseEntity<Map<String, Object>> fixAvatarPath(
            @RequestParam String avatarPath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String fixedPath = avatarSyncService.fixAvatarPath(avatarPath);
            
            response.put("success", true);
            response.put("originalPath", avatarPath);
            response.put("fixedPath", fixedPath);
            response.put("message", "头像路径已修复");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "修复失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 