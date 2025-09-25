package org.example.service;

import org.example.entity.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成就检测和通知服务
 */
@Service
public class AchievementDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementDetectionService.class);

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private UserLevelRepository userLevelRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 初始化默认成就
     */
    @Transactional
    public void initializeAchievements() {
        if (achievementRepository.count() > 0) {
            logger.info("成就已经初始化过了");
            return;
        }

        List<Achievement> achievements = createDefaultAchievements();
        achievementRepository.saveAll(achievements);
        logger.info("初始化了 {} 个默认成就", achievements.size());
    }

    /**
     * 创建默认成就列表
     */
    private List<Achievement> createDefaultAchievements() {
        List<Achievement> achievements = new ArrayList<>();

        // 基础成就
        achievements.add(new Achievement("初来乍到", "完成首次登录", 
            Achievement.AchievementCategory.BASIC, Achievement.AchievementRarity.COMMON, 
            10, "LEVEL", 1L));
        achievements.add(new Achievement("新手上路", "达到2级", 
            Achievement.AchievementCategory.BASIC, Achievement.AchievementRarity.COMMON, 
            20, "LEVEL", 2L));
        achievements.add(new Achievement("小有成就", "达到5级", 
            Achievement.AchievementCategory.BASIC, Achievement.AchievementRarity.UNCOMMON, 
            50, "LEVEL", 5L));
        achievements.add(new Achievement("渐入佳境", "达到10级", 
            Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.RARE, 
            100, "LEVEL", 10L));
        achievements.add(new Achievement("资深玩家", "达到20级", 
            Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.EPIC, 
            200, "LEVEL", 20L));
        achievements.add(new Achievement("传奇大师", "达到50级", 
            Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.LEGENDARY, 
            500, "LEVEL", 50L));

        // 上传相关成就
        achievements.add(new Achievement("首次分享", "上传第一个视频", 
            Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.COMMON, 
            25, "VIDEO_COUNT", 1L));
        achievements.add(new Achievement("内容创作者", "上传10个视频", 
            Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.UNCOMMON, 
            100, "VIDEO_COUNT", 10L));
        achievements.add(new Achievement("多产作者", "上传50个视频", 
            Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.RARE, 
            300, "VIDEO_COUNT", 50L));
        achievements.add(new Achievement("创作大师", "上传100个视频", 
            Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.EPIC, 
            500, "VIDEO_COUNT", 100L));

        // 社交相关成就
        achievements.add(new Achievement("受人喜爱", "获得100个点赞", 
            Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.UNCOMMON, 
            75, "LIKE_COUNT", 100L));
        achievements.add(new Achievement("人气之星", "获得500个点赞", 
            Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.RARE, 
            200, "LIKE_COUNT", 500L));
        achievements.add(new Achievement("超级明星", "获得1000个点赞", 
            Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.EPIC, 
            400, "LIKE_COUNT", 1000L));
        achievements.add(new Achievement("话痨达人", "发表100条评论", 
            Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.UNCOMMON, 
            50, "COMMENT_COUNT", 100L));

        // 观看相关成就
        achievements.add(new Achievement("观影爱好者", "观看时长达到10小时", 
            Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.COMMON, 
            30, "WATCH_TIME", 36000L)); // 10小时 = 36000秒
        achievements.add(new Achievement("资深观众", "观看时长达到100小时", 
            Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.RARE, 
            150, "WATCH_TIME", 360000L)); // 100小时
        achievements.add(new Achievement("超级粉丝", "观看时长达到500小时", 
            Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.EPIC, 
            400, "WATCH_TIME", 1800000L)); // 500小时

        // 特殊成就
        achievements.add(new Achievement("坚持不懈", "连续登录7天", 
            Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.UNCOMMON, 
            100, "CONSECUTIVE_DAYS", 7L));
        achievements.add(new Achievement("忠实用户", "连续登录30天", 
            Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.RARE, 
            300, "CONSECUTIVE_DAYS", 30L));
        achievements.add(new Achievement("铁杆粉丝", "连续登录100天", 
            Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.LEGENDARY, 
            1000, "CONSECUTIVE_DAYS", 100L));

        return achievements;
    }

    /**
     * 检测用户的所有成就（包括历史成就）
     */
    @Transactional
    public List<Achievement> detectAllUserAchievements(Long userId) {
        logger.info("开始检测用户 {} 的所有成就", userId);

        UserLevel userLevel = userLevelRepository.findByUserId(userId).orElse(null);
        if (userLevel == null) {
            logger.warn("用户 {} 没有等级信息，跳过成就检测", userId);
            return Collections.emptyList();
        }

        // 获取所有活跃的成就
        List<Achievement> allAchievements = achievementRepository.findByIsActiveTrue();
        
        // 获取用户已获得的成就
        Set<Long> userAchievementIds = userAchievementRepository
                .findByUserId(userId)
                .stream()
                .map(ua -> ua.getAchievement().getId())
                .collect(Collectors.toSet());

        List<Achievement> newAchievements = new ArrayList<>();

        // 检查每个成就
        for (Achievement achievement : allAchievements) {
            if (!userAchievementIds.contains(achievement.getId()) && 
                achievement.checkCondition(userLevel)) {
                
                // 授予成就
                grantAchievement(userId, achievement);
                newAchievements.add(achievement);
                
                logger.info("用户 {} 获得新成就：{}", userId, achievement.getName());
            }
        }

        if (!newAchievements.isEmpty()) {
            logger.info("用户 {} 总共获得了 {} 个新成就", userId, newAchievements.size());
        }

        return newAchievements;
    }

    /**
     * 授予成就并发送通知
     */
    @Transactional
    public void grantAchievement(Long userId, Achievement achievement) {
        // 检查是否已经拥有该成就
        if (userAchievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) {
            return;
        }

        // 创建用户成就记录
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUserId(userId);
        userAchievement.setAchievement(achievement);
        userAchievement.setUnlockedAt(LocalDateTime.now());
        userAchievementRepository.save(userAchievement);

        // 发送成就通知
        sendAchievementNotification(userId, achievement);

        // 发布成就获得事件
        publishAchievementEvent(userId, achievement);

        logger.info("成功授予用户 {} 成就：{}", userId, achievement.getName());
    }

    /**
     * 发送成就通知
     */
    private void sendAchievementNotification(Long userId, Achievement achievement) {
        try {
            String rarityEmoji = getRarityEmoji(achievement.getRarity());
            String title = String.format("🏆 恭喜获得%s成就！", rarityEmoji);
            String content = String.format("您获得了成就「%s」\n%s\n奖励：+%d 经验值", 
                    achievement.getName(), 
                    achievement.getDescription(),
                    achievement.getPoints());

            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(Notification.NotificationType.ACHIEVEMENT);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedId(achievement.getId());
            notification.setRelatedType("ACHIEVEMENT");
            
            notificationRepository.save(notification);
            
            logger.info("已发送成就通知给用户 {}: {}", userId, achievement.getName());
        } catch (Exception e) {
            logger.error("发送成就通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取稀有度对应的表情符号
     */
    private String getRarityEmoji(Achievement.AchievementRarity rarity) {
        switch (rarity) {
            case COMMON: return "🥉";
            case UNCOMMON: return "🥈";
            case RARE: return "🥇";
            case EPIC: return "💎";
            case LEGENDARY: return "👑";
            default: return "🏆";
        }
    }

    /**
     * 发布成就获得事件
     */
    private void publishAchievementEvent(Long userId, Achievement achievement) {
        try {
            AchievementUnlockedEvent event = new AchievementUnlockedEvent(userId, achievement);
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            logger.error("发布成就事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检测所有用户的历史成就
     */
    @Transactional
    public void detectAllUsersHistoricalAchievements() {
        logger.info("开始检测所有用户的历史成就");
        
        List<User> allUsers = userRepository.findAll();
        int totalNewAchievements = 0;
        
        for (User user : allUsers) {
            try {
                List<Achievement> newAchievements = detectAllUserAchievements(user.getId());
                totalNewAchievements += newAchievements.size();
            } catch (Exception e) {
                logger.error("检测用户 {} 的成就时出错: {}", user.getId(), e.getMessage());
            }
        }
        
        logger.info("历史成就检测完成，总共为所有用户授予了 {} 个成就", totalNewAchievements);
    }

    /**
     * 成就解锁事件
     */
    public static class AchievementUnlockedEvent {
        private final Long userId;
        private final Achievement achievement;
        private final LocalDateTime timestamp;

        public AchievementUnlockedEvent(Long userId, Achievement achievement) {
            this.userId = userId;
            this.achievement = achievement;
            this.timestamp = LocalDateTime.now();
        }

        public Long getUserId() { return userId; }
        public Achievement getAchievement() { return achievement; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
