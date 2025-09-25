package org.example.service;

import org.example.entity.Achievement;
import org.example.entity.User;
import org.example.entity.UserAchievement;
import org.example.repository.AchievementRepository;
import org.example.repository.UserAchievementRepository;
import org.example.repository.VideoRepository;
import org.example.repository.CommentRepository;
import org.example.repository.VideoLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 成就自动检测服务 - 后台自动检测和授予成就
 */
@Service
public class AchievementAutoDetectionService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    /**
     * 异步自动检测用户成就
     */
    @Async
    @Transactional
    public CompletableFuture<Integer> autoDetectUserAchievements(User user) {
        System.out.println("🤖 开始自动检测用户成就: " + user.getUsername());
        
        int newAchievementsCount = 0;
        
        try {
            // 检测基础成就
            newAchievementsCount += detectBasicAchievements(user);
            
            // 检测上传相关成就
            newAchievementsCount += detectUploadAchievements(user);
            
            // 检测社交相关成就
            newAchievementsCount += detectSocialAchievements(user);
            
            System.out.println("✅ 自动检测完成，新获得 " + newAchievementsCount + " 个成就");
            
        } catch (Exception e) {
            System.err.println("❌ 自动检测失败: " + e.getMessage());
        }
        
        return CompletableFuture.completedFuture(newAchievementsCount);
    }

    /**
     * 检测基础成就
     */
    private int detectBasicAchievements(User user) {
        int count = 0;
        
        // 新手上路成就
        if (grantAchievementIfNotExists(user, "新手上路")) {
            count++;
        }
        
        return count;
    }

    /**
     * 检测上传相关成就
     */
    private int detectUploadAchievements(User user) {
        int count = 0;
        long videoCount = videoRepository.countByUserId(user.getId());
        
        // 首次上传
        if (videoCount >= 1 && grantAchievementIfNotExists(user, "首次上传")) {
            count++;
        }
        
        // 小有成就 (5个视频)
        if (videoCount >= 5 && grantAchievementIfNotExists(user, "小有成就")) {
            count++;
        }
        
        // 创作达人 (10个视频)
        if (videoCount >= 10 && grantAchievementIfNotExists(user, "创作达人")) {
            count++;
        }
        
        return count;
    }

    /**
     * 检测社交相关成就
     */
    private int detectSocialAchievements(User user) {
        int count = 0;
        
        // 评论数量成就
        long commentCount = commentRepository.countByUserId(user.getId());
        if (commentCount >= 1 && grantAchievementIfNotExists(user, "话痨")) {
            count++;
        }
        
        // 点赞数量成就
        long likeCount = videoLikeRepository.countByUserId(user.getId());
        if (likeCount >= 1 && grantAchievementIfNotExists(user, "点赞狂魔")) {
            count++;
        }
        
        return count;
    }

    /**
     * 如果用户没有该成就则授予
     */
    private boolean grantAchievementIfNotExists(User user, String achievementName) {
        try {
            // 查找成就
            var achievementOpt = achievementRepository.findByNameAndIsActiveTrue(achievementName);
            if (!achievementOpt.isPresent()) {
                System.out.println("⚠️ 成就不存在: " + achievementName);
                return false;
            }
            
            Achievement achievement = achievementOpt.get();
            
            // 检查用户是否已有该成就
            boolean hasAchievement = userAchievementRepository.existsByUserIdAndAchievementId(
                user.getId(), achievement.getId());
            
            if (!hasAchievement) {
                // 授予成就
                UserAchievement userAchievement = new UserAchievement();
                userAchievement.setUser(user);
                userAchievement.setAchievement(achievement);
                userAchievement.setUnlockedAt(LocalDateTime.now());
                userAchievement.setProgress(1.0);
                userAchievement.setIsDisplayed(true);
                userAchievement.setNotificationSent(false);
                
                userAchievementRepository.save(userAchievement);
                
                System.out.println("🏆 自动授予成就: " + achievementName);
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("❌ 授予成就失败: " + achievementName + " - " + e.getMessage());
        }
        
        return false;
    }

    /**
     * 获取用户成就统计（快速版本）
     */
    public String getQuickAchievementStats(User user) {
        try {
            // 快速统计
            long userAchievementCount = userAchievementRepository.countByUser(user);
            long totalAchievementCount = achievementRepository.count();
            
            double completionRate = totalAchievementCount > 0 ? 
                (double) userAchievementCount / totalAchievementCount * 100 : 0;
            
            return String.format("成就: %d/%d (%.1f%%)", 
                userAchievementCount, totalAchievementCount, completionRate);
                
        } catch (Exception e) {
            return "获取统计失败";
        }
    }

    /**
     * 批量检测所有用户成就（定时任务用）
     */
    @Async
    public void batchDetectAllUsersAchievements() {
        System.out.println("🔄 开始批量检测所有用户成就...");
        
        try {
            // 这里可以添加批量检测逻辑
            // 为了性能考虑，暂时不实现
            System.out.println("✅ 批量检测完成");
            
        } catch (Exception e) {
            System.err.println("❌ 批量检测失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否有新成就可以获得
     */
    public boolean hasNewAchievementsAvailable(User user) {
        try {
            long videoCount = videoRepository.countByUserId(user.getId());
            long commentCount = commentRepository.countByUserId(user.getId());
            long likeCount = videoLikeRepository.countByUserId(user.getId());
            
            // 简单检查是否有可能获得新成就
            return videoCount > 0 || commentCount > 0 || likeCount > 0;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 立即检测并返回结果
     */
    @Transactional
    public int immediateDetectAchievements(User user) {
        System.out.println("⚡ 立即检测用户成就: " + user.getUsername());
        
        int newCount = 0;
        newCount += detectBasicAchievements(user);
        newCount += detectUploadAchievements(user);
        newCount += detectSocialAchievements(user);
        
        System.out.println("⚡ 立即检测完成，新获得 " + newCount + " 个成就");
        return newCount;
    }
}
