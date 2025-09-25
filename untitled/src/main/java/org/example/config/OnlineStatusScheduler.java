package org.example.config;

import org.example.service.UserOnlineStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 在线状态定时任务
 */
@Component
public class OnlineStatusScheduler {

    @Autowired
    private UserOnlineStatusService userOnlineStatusService;

    /**
     * 每分钟清理一次过期的在线状态
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void cleanupExpiredUsers() {
        try {
            userOnlineStatusService.cleanupExpiredUsers();
        } catch (Exception e) {
            System.err.println("清理过期用户状态时出错: " + e.getMessage());
        }
    }

    /**
     * 每30秒广播一次在线用户数量
     */
    @Scheduled(fixedRate = 30000) // 30秒
    public void broadcastOnlineCount() {
        try {
            userOnlineStatusService.broadcastOnlineCount();
        } catch (Exception e) {
            System.err.println("广播在线用户数量时出错: " + e.getMessage());
        }
    }
}
