package org.example.controller;

import org.example.service.DatabaseRepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库修复控制器
 * 用于修复数据一致性问题
 */
@RestController
@RequestMapping("/api/admin/database")
public class DatabaseRepairController {

    @Autowired
    private DatabaseRepairService databaseRepairService;

    /**
     * 修复关注关系数据
     */
    @PostMapping("/repair/follows")
    public ResponseEntity<Map<String, Object>> repairFollowRelationships() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔧 管理员触发关注关系数据修复...");
            
            // 先打印统计信息
            databaseRepairService.printDatabaseStats();
            
            // 执行修复
            Map<String, Object> result = databaseRepairService.repairFollows();
            
            // 修复后再次打印统计信息
            databaseRepairService.printDatabaseStats();
            
            response.put("success", true);
            response.put("message", "关注关系数据修复完成");
            response.putAll(result);
            
        } catch (Exception e) {
            System.err.println("❌ 数据库修复失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "数据库修复失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取数据库统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            databaseRepairService.printDatabaseStats();
            
            response.put("success", true);
            response.put("message", "数据库统计信息已打印到控制台");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 