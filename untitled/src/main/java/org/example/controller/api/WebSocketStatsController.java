package org.example.controller.api;

import org.example.config.WebSocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket统计API控制器
 */
@RestController
@RequestMapping("/api/websocket")
public class WebSocketStatsController {

    @Autowired
    private WebSocketConfig webSocketConfig;

    /**
     * 获取WebSocket连接统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getWebSocketStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取基本统计信息
            Map<String, Object> connectionStats = webSocketConfig.getConnectionStats();
            stats.putAll(connectionStats);
            
            // 添加系统信息
            stats.put("systemInfo", getSystemInfo());
            stats.put("timestamp", System.currentTimeMillis());
            stats.put("status", "success");
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "获取统计信息失败: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取系统信息
     */
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("freeMemory", runtime.freeMemory());
        systemInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        
        return systemInfo;
    }
} 