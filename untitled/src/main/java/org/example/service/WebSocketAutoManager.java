package org.example.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Service
public class WebSocketAutoManager {
    
    private static final Logger logger = Logger.getLogger(WebSocketAutoManager.class.getName());
    
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionTimestamps = new ConcurrentHashMap<>();
    
    private static final int MAX_CONNECTIONS = 1000;
    private static final int WARNING_THRESHOLD = 800;
    private static final int CRITICAL_THRESHOLD = 950;
    private static final long CONNECTION_TIMEOUT = 120000; // 2分钟超时
    private static final long CLEANUP_INTERVAL = 15000; // 15秒清理一次
    
    /**
     * 自动清理超时连接
     */
    @Scheduled(fixedRate = CLEANUP_INTERVAL)
    public void autoCleanupConnections() {
        try {
            long currentTime = System.currentTimeMillis();
            int cleanedCount = 0;
            
            // 清理超时连接
            for (Map.Entry<String, Long> entry : sessionTimestamps.entrySet()) {
                String sessionId = entry.getKey();
                Long timestamp = entry.getValue();
                
                if (currentTime - timestamp > CONNECTION_TIMEOUT) {
                    WebSocketSession session = sessions.get(sessionId);
                    if (session != null && session.isOpen()) {
                        try {
                            session.close();
                            logger.info("自动清理超时连接: " + sessionId);
                        } catch (IOException e) {
                            logger.warning("关闭超时连接失败: " + sessionId + " - " + e.getMessage());
                        }
                    }
                    sessions.remove(sessionId);
                    sessionTimestamps.remove(sessionId);
                    cleanedCount++;
                }
            }
            
            if (cleanedCount > 0) {
                logger.info("自动清理完成: " + cleanedCount + " 个超时连接被清理");
                updateConnectionCounts();
            }
            
            // 检查连接数并发出警告
            checkConnectionThresholds();
            
        } catch (Exception e) {
            logger.severe("自动清理过程中发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 检查连接数阈值并自动处理
     */
    private void checkConnectionThresholds() {
        int currentConnections = activeConnections.get();
        
        if (currentConnections >= CRITICAL_THRESHOLD) {
            logger.severe("🚨 连接数达到危险阈值: " + currentConnections + "/" + MAX_CONNECTIONS);
            emergencyCleanup();
        } else if (currentConnections >= WARNING_THRESHOLD) {
            logger.warning("⚠️ 连接数达到警告阈值: " + currentConnections + "/" + MAX_CONNECTIONS);
            aggressiveCleanup();
        }
    }
    
    /**
     * 紧急清理 - 清理所有可能的连接
     */
    private void emergencyCleanup() {
        logger.warning("执行紧急连接清理...");
        
        // 清理所有超时连接
        long currentTime = System.currentTimeMillis();
        int emergencyCleaned = 0;
        
        for (Map.Entry<String, Long> entry : sessionTimestamps.entrySet()) {
            String sessionId = entry.getKey();
            Long timestamp = entry.getValue();
            
            // 清理超过30秒的连接
            if (currentTime - timestamp > 30000) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        session.close();
                        emergencyCleaned++;
                    } catch (IOException e) {
                        logger.warning("紧急清理时关闭连接失败: " + sessionId);
                    }
                }
                sessions.remove(sessionId);
                sessionTimestamps.remove(sessionId);
            }
        }
        
        logger.info("紧急清理完成: " + emergencyCleaned + " 个连接被清理");
        updateConnectionCounts();
    }
    
    /**
     * 积极清理 - 清理较老的连接
     */
    private void aggressiveCleanup() {
        logger.info("执行积极连接清理...");
        
        long currentTime = System.currentTimeMillis();
        int aggressiveCleaned = 0;
        
        for (Map.Entry<String, Long> entry : sessionTimestamps.entrySet()) {
            String sessionId = entry.getKey();
            Long timestamp = entry.getValue();
            
            // 清理超过1分钟的连接
            if (currentTime - timestamp > 60000) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        session.close();
                        aggressiveCleaned++;
                    } catch (IOException e) {
                        logger.warning("积极清理时关闭连接失败: " + sessionId);
                    }
                }
                sessions.remove(sessionId);
                sessionTimestamps.remove(sessionId);
            }
        }
        
        logger.info("积极清理完成: " + aggressiveCleaned + " 个连接被清理");
        updateConnectionCounts();
    }
    
    /**
     * 注册新连接
     */
    public void registerConnection(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        sessionTimestamps.put(sessionId, System.currentTimeMillis());
        updateConnectionCounts();
        
        logger.info("新连接注册: " + sessionId + " (当前连接数: " + activeConnections.get() + ")");
    }
    
    /**
     * 注销连接
     */
    public void unregisterConnection(String sessionId) {
        sessions.remove(sessionId);
        sessionTimestamps.remove(sessionId);
        updateConnectionCounts();
        
        logger.info("连接注销: " + sessionId + " (当前连接数: " + activeConnections.get() + ")");
    }
    
    /**
     * 更新连接计数
     */
    private void updateConnectionCounts() {
        totalConnections.set(sessions.size());
        activeConnections.set((int) sessions.values().stream()
                .filter(WebSocketSession::isOpen)
                .count());
    }
    
    /**
     * 获取连接统计信息
     */
    public Map<String, Object> getConnectionStats() {
        updateConnectionCounts();
        
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalConnections", totalConnections.get());
        stats.put("activeConnections", activeConnections.get());
        stats.put("maxConnections", MAX_CONNECTIONS);
        stats.put("warningThreshold", WARNING_THRESHOLD);
        stats.put("criticalThreshold", CRITICAL_THRESHOLD);
        stats.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return stats;
    }
    
    /**
     * 手动清理连接
     */
    public Map<String, Object> manualCleanup() {
        int beforeCount = activeConnections.get();
        autoCleanupConnections();
        int afterCount = activeConnections.get();
        
        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("success", true);
        result.put("cleanedCount", beforeCount - afterCount);
        result.put("beforeCount", beforeCount);
        result.put("afterCount", afterCount);
        result.put("message", "手动清理完成");
        
        return result;
    }
    
    /**
     * 强制重置所有连接
     */
    public Map<String, Object> forceReset() {
        logger.warning("执行强制连接重置...");
        
        int beforeCount = sessions.size();
        
        // 关闭所有连接
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.close();
                } catch (IOException e) {
                    logger.warning("强制重置时关闭连接失败: " + e.getMessage());
                }
            }
        }
        
        // 清空所有记录
        sessions.clear();
        sessionTimestamps.clear();
        updateConnectionCounts();
        
        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("success", true);
        result.put("resetCount", beforeCount);
        result.put("currentCount", activeConnections.get());
        result.put("message", "强制重置完成");
        
        logger.info("强制重置完成: " + beforeCount + " 个连接被重置");
        
        return result;
    }
} 