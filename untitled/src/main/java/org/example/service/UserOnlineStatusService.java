package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 用户在线状态管理服务
 */
@Service
public class UserOnlineStatusService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 存储在线用户信息：userId -> 最后活跃时间
    private final Map<Long, LocalDateTime> onlineUsers = new ConcurrentHashMap<>();
    
    // 存储用户会话信息：userId -> sessionId
    private final Map<Long, String> userSessions = new ConcurrentHashMap<>();

    /**
     * 用户上线
     */
    public void userOnline(Long userId, String sessionId) {
        boolean wasOffline = !onlineUsers.containsKey(userId);
        
        onlineUsers.put(userId, LocalDateTime.now());
        userSessions.put(userId, sessionId);
        
        System.out.println("用户上线: " + userId + ", 会话ID: " + sessionId);
        
        // 如果用户之前是离线状态，广播上线通知
        if (wasOffline) {
            broadcastUserStatusChange(userId, true);
        }
    }

    /**
     * 用户下线
     */
    public void userOffline(Long userId) {
        boolean wasOnline = onlineUsers.containsKey(userId);
        
        onlineUsers.remove(userId);
        userSessions.remove(userId);
        
        System.out.println("用户下线: " + userId);
        
        // 如果用户之前是在线状态，广播下线通知
        if (wasOnline) {
            broadcastUserStatusChange(userId, false);
        }
    }

    /**
     * 更新用户活跃时间
     */
    public void updateUserActivity(Long userId) {
        if (onlineUsers.containsKey(userId)) {
            onlineUsers.put(userId, LocalDateTime.now());
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * 获取所有在线用户ID
     */
    public Set<Long> getOnlineUserIds() {
        return onlineUsers.keySet();
    }

    /**
     * 定时清理过期的在线状态（超过1分钟无活动）
     */
    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void cleanupExpiredUsers() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(1);

        Set<Long> expiredUsers = onlineUsers.entrySet().stream()
            .filter(entry -> entry.getValue().isBefore(expireTime))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        for (Long userId : expiredUsers) {
            userOffline(userId);
        }

        if (!expiredUsers.isEmpty()) {
            System.out.println("清理过期用户: " + expiredUsers + "，当前在线用户数: " + getOnlineUserCount());
            broadcastOnlineCount();
        }
    }

    /**
     * 广播用户状态变化
     */
    private void broadcastUserStatusChange(Long userId, boolean isOnline) {
        try {
            Map<String, Object> statusUpdate = Map.of(
                "type", "user_status_change",
                "userId", userId,
                "isOnline", isOnline,
                "timestamp", System.currentTimeMillis()
            );
            
            // 广播到所有用户
            messagingTemplate.convertAndSend("/topic/user-status", statusUpdate);
            
            System.out.println("广播用户状态变化: 用户" + userId + " " + (isOnline ? "上线" : "下线"));
        } catch (Exception e) {
            System.err.println("广播用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 广播在线用户数量更新
     */
    public void broadcastOnlineCount() {
        try {
            Map<String, Object> countUpdate = Map.of(
                "type", "online_count_update",
                "count", getOnlineUserCount(),
                "timestamp", System.currentTimeMillis()
            );

            messagingTemplate.convertAndSend("/topic/online-count", countUpdate);
            System.out.println("广播在线用户数量更新: " + getOnlineUserCount());
        } catch (Exception e) {
            System.err.println("广播在线用户数量失败: " + e.getMessage());
        }
    }



    /**
     * 定时广播在线用户数量更新（确保前端同步）
     */
    @Scheduled(fixedRate = 15000) // 每15秒广播一次
    public void periodicBroadcast() {
        int currentCount = getOnlineUserCount();
        if (currentCount > 0) {
            broadcastOnlineCount();
        }
    }
}
