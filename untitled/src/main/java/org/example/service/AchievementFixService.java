package org.example.service;

import org.example.entity.Achievement;
import org.example.entity.User;
import org.example.entity.UserAchievement;
import org.example.entity.UserLevel;
import org.example.repository.AchievementRepository;
import org.example.repository.UserAchievementRepository;
import org.example.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 成就修复服务 - 专门用于快速修复和触发成就
 */
@Service
public class AchievementFixService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserLevelService userLevelService;

    /**
     * 快速检查并授予基础成就
     */
    @Transactional
    public void quickFixUserAchievements(User user) {
        System.out.println("🔧 开始快速修复用户成就: " + user.getUsername());

        try {
            // 1. 检查首次上传成就
            checkFirstUploadAchievement(user);

            // 2. 检查注册成就
            checkRegistrationAchievement(user);

            // 3. 同步用户统计
            userLevelService.syncUserStats(user);

            System.out.println("✅ 成就修复完成");

        } catch (Exception e) {
            System.err.println("❌ 成就修复失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查首次上传成就
     */
    private void checkFirstUploadAchievement(User user) {
        try {
            // 查找"首次上传"成就
            Optional<Achievement> firstUploadAchievement = achievementRepository.findByNameAndIsActiveTrue("首次上传");
            if (!firstUploadAchievement.isPresent()) {
                System.out.println("⚠️ 未找到'首次上传'成就");
                return;
            }

            Achievement achievement = firstUploadAchievement.get();

            // 检查用户是否已经有这个成就
            boolean hasAchievement = userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId());
            if (hasAchievement) {
                System.out.println("✅ 用户已有'首次上传'成就");
                return;
            }

            // 检查用户是否有上传的视频
            long videoCount = videoRepository.countByUserId(user.getId());
            if (videoCount > 0) {
                // 授予成就
                grantAchievement(user, achievement);
                System.out.println("🎉 授予'首次上传'成就成功！");
            } else {
                System.out.println("📹 用户还没有上传视频，无法获得'首次上传'成就");
            }

        } catch (Exception e) {
            System.err.println("❌ 检查首次上传成就失败: " + e.getMessage());
        }
    }

    /**
     * 检查注册成就
     */
    private void checkRegistrationAchievement(User user) {
        try {
            // 查找"新手上路"成就
            Optional<Achievement> registrationAchievement = achievementRepository.findByNameAndIsActiveTrue("新手上路");
            if (!registrationAchievement.isPresent()) {
                System.out.println("⚠️ 未找到'新手上路'成就");
                return;
            }

            Achievement achievement = registrationAchievement.get();

            // 检查用户是否已经有这个成就
            boolean hasAchievement = userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId());
            if (!hasAchievement) {
                // 授予成就
                grantAchievement(user, achievement);
                System.out.println("🎉 授予'新手上路'成就成功！");
            } else {
                System.out.println("✅ 用户已有'新手上路'成就");
            }

        } catch (Exception e) {
            System.err.println("❌ 检查注册成就失败: " + e.getMessage());
        }
    }

    /**
     * 授予成就
     */
    private void grantAchievement(User user, Achievement achievement) {
        try {
            UserAchievement userAchievement = new UserAchievement();
            userAchievement.setUser(user);
            userAchievement.setAchievement(achievement);
            userAchievement.setUnlockedAt(LocalDateTime.now());
            userAchievement.setProgress(1.0); // 100%完成
            userAchievement.setIsDisplayed(true);
            userAchievement.setNotificationSent(false);

            userAchievementRepository.save(userAchievement);

            System.out.println("🏆 成功授予成就: " + achievement.getName());

        } catch (Exception e) {
            System.err.println("❌ 授予成就失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取用户成就统计
     */
    public String getUserAchievementStats(User user) {
        try {
            List<UserAchievement> userAchievements = userAchievementRepository.findByUserOrderByUnlockedAtDesc(user);
            List<Achievement> allAchievements = achievementRepository.findByIsActiveTrue();

            int totalAchievements = allAchievements.size();
            int unlockedAchievements = userAchievements.size();
            double completionRate = totalAchievements > 0 ? (double) unlockedAchievements / totalAchievements * 100 : 0;

            return String.format("成就统计: %d/%d (%.1f%%)", unlockedAchievements, totalAchievements, completionRate);

        } catch (Exception e) {
            return "获取成就统计失败: " + e.getMessage();
        }
    }

    /**
     * 列出用户的所有成就
     */
    public void listUserAchievements(User user) {
        try {
            List<UserAchievement> userAchievements = userAchievementRepository.findByUserOrderByUnlockedAtDesc(user);

            System.out.println("=== 用户成就列表 ===");
            if (userAchievements.isEmpty()) {
                System.out.println("❌ 用户还没有获得任何成就");
            } else {
                for (UserAchievement ua : userAchievements) {
                    System.out.println("🏆 " + ua.getAchievement().getName() + " - " + ua.getUnlockedAt());
                }
            }
            System.out.println("==================");

        } catch (Exception e) {
            System.err.println("❌ 列出用户成就失败: " + e.getMessage());
        }
    }

    /**
     * 强制触发所有基础成就检查
     */
    @Transactional
    public void forceCheckAllBasicAchievements(User user) {
        System.out.println("🚀 强制检查所有基础成就...");

        // 检查注册成就
        checkRegistrationAchievement(user);

        // 检查上传成就
        checkFirstUploadAchievement(user);

        // 检查其他基础成就
        checkBasicAchievements(user);

        System.out.println("✅ 所有基础成就检查完成");
    }

    /**
     * 检查其他基础成就
     */
    private void checkBasicAchievements(User user) {
        try {
            // 可以在这里添加更多基础成就的检查逻辑
            System.out.println("📋 检查其他基础成就...");

        } catch (Exception e) {
            System.err.println("❌ 检查基础成就失败: " + e.getMessage());
        }
    }
}
