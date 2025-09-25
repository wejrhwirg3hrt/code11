package org.example.service;

import org.example.entity.UserLoginLog;
import org.example.repository.UserLoginLogRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = false)
public class UserLoginLogService {

    @Autowired
    private UserLoginLogRepository loginLogRepository;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserRepository userRepository;

    public void recordLogin(Long userId, String username, String ip, String userAgent) {
        UserLoginLog log = new UserLoginLog(userId, username, ip, userAgent);

        // 这里可以集成IP地理位置服务来获取位置信息
        // 示例：使用免费的IP地理位置API
        try {
            Map<String, Object> location = getLocationFromIP(ip);
            if (location != null) {
                log.setLoginLocation((String) location.get("city"));
                log.setLatitude((Double) location.get("latitude"));
                log.setLongitude((Double) location.get("longitude"));
            }
        } catch (Exception e) {
            // 如果获取位置失败，记录日志但不影响登录
            System.err.println("获取IP位置失败: " + e.getMessage());
        }

        loginLogRepository.save(log);

        // 触发登录相关成就检查
        try {
            userRepository.findById(userId).ifPresent(user -> {
                // 计算连续登录天数
                int consecutiveDays = calculateConsecutiveDays(userId);
                achievementService.triggerAchievementCheck(user, "LOGIN", 1);
                achievementService.triggerAchievementCheck(user, "CONSECUTIVE_DAYS", consecutiveDays);
            });
        } catch (Exception e) {
            System.err.println("❌ 登录成就检查失败: " + e.getMessage());
        }
    }

    public List<UserLoginLog> getLoginLocations() {
        return loginLogRepository.getLoginLocations();
    }

    public Map<String, Object> getLoginStatistics() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusDays(30);

        stats.put("todayLogins", loginLogRepository.countByLoginTimeBetween(todayStart, now));
        stats.put("weekLogins", loginLogRepository.countByLoginTimeBetween(weekStart, now));
        stats.put("monthLogins", loginLogRepository.countByLoginTimeBetween(monthStart, now));
        stats.put("totalLogins", loginLogRepository.count());

        // 获取登录地点统计
        List<Object[]> locationStats = loginLogRepository.getLoginLocationStats();
        Map<String, Long> locationMap = new HashMap<>();
        for (Object[] stat : locationStats) {
            locationMap.put((String) stat[0], (Long) stat[1]);
        }
        stats.put("locationStats", locationMap);

        return stats;
    }

    public List<UserLoginLog> getUserLoginHistory(Long userId) {
        return loginLogRepository.findByUserIdOrderByLoginTimeDesc(userId);
    }

    private Map<String, Object> getLocationFromIP(String ip) {
        // 这里是一个简化的示例，实际项目中可以使用：
        // 1. 免费服务：ip-api.com, ipapi.co
        // 2. 付费服务：MaxMind GeoIP2, IPStack
        // 3. 本地数据库：GeoLite2

        Map<String, Object> location = new HashMap<>();

        // 示例数据（实际应该调用真实的地理位置API）
        if ("127.0.0.1".equals(ip) || "localhost".equals(ip)) {
            location.put("city", "本地");
            location.put("latitude", 39.9042);
            location.put("longitude", 116.4074);
        } else {
            // 这里可以集成真实的IP地理位置服务
            location.put("city", "未知");
            location.put("latitude", 0.0);
            location.put("longitude", 0.0);
        }

        return location;
    }

    /**
     * 计算用户连续登录天数
     */
    private int calculateConsecutiveDays(Long userId) {
        try {
            List<UserLoginLog> recentLogins = loginLogRepository.findByUserIdOrderByLoginTimeDesc(userId);
            if (recentLogins.isEmpty()) {
                return 1; // 第一次登录
            }

            int consecutiveDays = 1;
            LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime checkDate = today.minusDays(1);

            for (UserLoginLog log : recentLogins) {
                LocalDateTime loginDate = log.getLoginTime().toLocalDate().atStartOfDay();

                if (loginDate.equals(checkDate)) {
                    consecutiveDays++;
                    checkDate = checkDate.minusDays(1);
                } else if (loginDate.isBefore(checkDate)) {
                    break; // 不连续了
                }
            }

            return consecutiveDays;
        } catch (Exception e) {
            System.err.println("❌ 计算连续登录天数失败: " + e.getMessage());
            return 1;
        }
    }
}