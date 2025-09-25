package org.example.controller;

import org.example.service.DatabaseFixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库管理控制器
 * 提供数据库维护和修复功能
 */
@RestController
@RequestMapping("/api/admin/database")
@PreAuthorize("hasRole('ADMIN')")
public class DatabaseManagementController {

    @Autowired
    private DatabaseFixService databaseFixService;

    /**
     * 修复所有数据库问题
     */
    @PostMapping("/fix")
    public ResponseEntity<Map<String, Object>> fixDatabaseIssues() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            databaseFixService.fixAllDatabaseIssues();
            
            response.put("success", true);
            response.put("message", "数据库问题修复完成");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "修复数据库问题时出错: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 修复daily_tasks表的日期时间值
     */
    @PostMapping("/fix/daily-tasks")
    public ResponseEntity<Map<String, Object>> fixDailyTasksDateTime() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            databaseFixService.fixDailyTasksDateTime();
            
            response.put("success", true);
            response.put("message", "daily_tasks表日期时间值修复完成");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "修复daily_tasks表时出错: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取数据库状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 这里可以添加更多数据库状态检查逻辑
            response.put("success", true);
            response.put("message", "数据库状态正常");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取数据库状态时出错: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 