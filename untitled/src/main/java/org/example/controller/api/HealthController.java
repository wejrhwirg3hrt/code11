package org.example.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.service.WebSocketAutoManager;
import java.util.Map;
import java.util.HashMap;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private WebSocketAutoManager autoManager;

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "Chat Application");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * WebSocket连接统计信息
     */
    @GetMapping("/health/websocket-stats")
    public ResponseEntity<Map<String, Object>> getWebSocketStats() {
        return ResponseEntity.ok(autoManager.getConnectionStats());
    }

    /**
     * 清理超时连接
     */
    @PostMapping("/health/cleanup-connections")
    public ResponseEntity<Map<String, Object>> cleanupConnections() {
        return ResponseEntity.ok(autoManager.manualCleanup());
    }

    /**
     * 重置WebSocket连接计数
     */
    @PostMapping("/health/reset-connections")
    public ResponseEntity<Map<String, Object>> resetConnections() {
        return ResponseEntity.ok(autoManager.forceReset());
    }
    
    /**
     * 获取自动管理器状态
     */
    @GetMapping("/health/auto-manager-status")
    public ResponseEntity<Map<String, Object>> getAutoManagerStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("autoManagerEnabled", true);
        response.put("cleanupInterval", "15秒");
        response.put("connectionTimeout", "2分钟");
        response.put("maxConnections", 1000);
        response.put("warningThreshold", 800);
        response.put("criticalThreshold", 950);
        response.put("message", "自动连接管理器已启用");
        return ResponseEntity.ok(response);
    }
} 