package org.example.service;

import org.example.entity.Achievement;
import org.example.entity.User;
import org.example.entity.UserAchievement;
import org.example.repository.*;
import org.example.repository.AchievementRepository;
import org.example.repository.UserAchievementRepository;
import org.example.service.NotificationService;
import org.example.service.UserLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 成就系统服务
 */
@Service
@Transactional(readOnly = false)
public class AchievementService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ViewHistoryRepository viewHistoryRepository;

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserLevelService userLevelService;

    /**
     * 获取所有成就
     */
    public List<Achievement> getAllAchievements() {
        // 临时修复：返回所有成就，包括非活跃的
        return achievementRepository.findAll();
    }

    /**
     * 获取所有成就（包括非活跃的，用于调试）
     */
    public List<Achievement> getAllAchievementsIncludingInactive() {
        return achievementRepository.findAll();
    }

    /**
     * 获取用户已解锁的成就
     */
    public List<UserAchievement> getUserAchievements(User user) {
        return userAchievementRepository.findByUserOrderByUnlockedAtDesc(user);
    }

    /**
     * 获取用户显示的成就（公开显示）
     */
    public List<UserAchievement> getDisplayedUserAchievements(User user) {
        return userAchievementRepository.findByUserAndIsDisplayedTrueOrderByUnlockedAtDesc(user);
    }

    /**
     * 获取最近解锁的成就
     */
    public List<UserAchievement> getRecentUserAchievements(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userAchievementRepository.findByUserOrderByUnlockedAtDesc(user, pageable);
    }

    /**
     * 解锁成就
     */
    public UserAchievement unlockAchievement(User user, Long achievementId) {
        Optional<Achievement> achievementOpt = achievementRepository.findById(achievementId);
        if (!achievementOpt.isPresent()) {
            throw new RuntimeException("成就不存在");
        }

        Achievement achievement = achievementOpt.get();
        
        // 检查是否已经解锁
        Optional<UserAchievement> existingOpt = userAchievementRepository.findByUserAndAchievement(user, achievement);
        if (existingOpt.isPresent()) {
            return existingOpt.get(); // 已经解锁，返回现有记录
        }

        // 创建新的用户成就记录
        UserAchievement userAchievement = new UserAchievement(user, achievement);
        UserAchievement savedAchievement = userAchievementRepository.save(userAchievement);

        // 给用户添加经验值奖励
        try {
            int expReward = achievement.getPoints(); // 成就积分作为经验值奖励
            userLevelService.addExperience(user, (long) expReward);
            System.out.println("🎯 成就经验值奖励已添加: " + expReward + " 经验值");
        } catch (Exception e) {
            System.err.println("❌ 添加成就经验值失败: " + e.getMessage());
        }

        // 发送成就解锁通知
        try {
            createAchievementNotification(user, achievement);
            System.out.println("🎉 成就解锁通知已发送: " + achievement.getName());
        } catch (Exception e) {
            System.err.println("❌ 发送成就通知失败: " + e.getMessage());
        }

        return savedAchievement;
    }

    /**
     * 检查并解锁成就
     */
    public void checkAndUnlockAchievements(User user, String triggerType, Object... params) {
        List<Achievement> allAchievements = getAllAchievements();
        
        for (Achievement achievement : allAchievements) {
            if (shouldUnlockAchievement(user, achievement, triggerType, params)) {
                try {
                    unlockAchievement(user, achievement.getId());
                } catch (Exception e) {
                    // 忽略已解锁的成就
                }
            }
        }
    }

    /**
     * 判断是否应该解锁成就
     */
    private boolean shouldUnlockAchievement(User user, Achievement achievement, String triggerType, Object... params) {
        // 检查用户是否已经解锁了这个成就
        Optional<UserAchievement> existingAchievement = userAchievementRepository.findByUserAndAchievement(user, achievement);
        if (existingAchievement.isPresent()) {
            return false; // 已经解锁，不需要重复解锁
        }

        // 根据成就的条件类型来判断
        String conditionType = achievement.getConditionType();
        Long conditionValue = achievement.getConditionValue();

        switch (conditionType) {
            // 注册相关
            case "REGISTER":
                return "REGISTER".equals(triggerType);

            // 上传视频相关
            case "UPLOAD_VIDEO":
                return "UPLOAD_VIDEO".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 点赞视频相关
            case "LIKE_VIDEO":
                return "LIKE_VIDEO".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 评论相关
            case "COMMENT":
                return "COMMENT".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 观看视频相关
            case "WATCH_VIDEO":
                return "WATCH_VIDEO".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 观看时长相关
            case "WATCH_TIME":
                return "WATCH_TIME".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 连续登录相关
            case "CONSECUTIVE_DAYS":
                return "CONSECUTIVE_DAYS".equals(triggerType) && params.length > 0 &&
                       ((Integer) params[0]) >= conditionValue;

            // 获得点赞相关
            case "RECEIVE_LIKE":
                return "RECEIVE_LIKE".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 关注用户相关
            case "FOLLOW_USER":
                return "FOLLOW_USER".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 获得关注相关
            case "RECEIVE_FOLLOW":
                return "RECEIVE_FOLLOW".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 上传时间相关
            case "UPLOAD_TIME":
                return "UPLOAD_TIME".equals(triggerType) && params.length > 0 &&
                       ((Integer) params[0]).equals(conditionValue.intValue());

            // 周末上传相关
            case "WEEKEND_UPLOAD":
                return "WEEKEND_UPLOAD".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 节假日上传相关
            case "HOLIDAY_UPLOAD":
                return "HOLIDAY_UPLOAD".equals(triggerType);

            // 分类多样性相关
            case "CATEGORY_DIVERSITY":
                return "CATEGORY_DIVERSITY".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 视频时长相关
            case "SHORT_VIDEO":
                return "SHORT_VIDEO".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            case "LONG_VIDEO":
                return "LONG_VIDEO".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            case "MARATHON_VIDEO":
                return "MARATHON_VIDEO".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 平均点赞相关
            case "AVG_LIKES":
                return "AVG_LIKES".equals(triggerType) && params.length > 0 &&
                       ((Double) params[0]) >= conditionValue;

            // 总点赞里程碑
            case "TOTAL_LIKES":
                return "TOTAL_LIKES".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 视频数量相关
            case "VIDEO_COUNT":
                return "VIDEO_COUNT".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 点赞数量相关
            case "LIKE_COUNT":
                return "LIKE_COUNT".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 评论数量相关
            case "COMMENT_COUNT":
                return "COMMENT_COUNT".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 分享数量相关
            case "SHARE_COUNT":
                return "SHARE_COUNT".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 收藏数量相关
            case "FAVORITE_COUNT":
                return "FAVORITE_COUNT".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 等级相关
            case "LEVEL":
                return "LEVEL".equals(triggerType) && params.length > 0 &&
                       ((Integer) params[0]) >= conditionValue.intValue();

            // 早期上传相关
            case "EARLY_UPLOAD":
                return "EARLY_UPLOAD".equals(triggerType);

            // 深夜上传相关
            case "LATE_UPLOAD":
                return "LATE_UPLOAD".equals(triggerType);

            // 生日登录相关
            case "BIRTHDAY_LOGIN":
                return "BIRTHDAY_LOGIN".equals(triggerType);

            // 完美评分相关
            case "PERFECT_RATING":
                return "PERFECT_RATING".equals(triggerType);

            // 病毒视频相关
            case "VIRAL_VIDEO":
                return "VIRAL_VIDEO".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 关注数量相关
            case "FOLLOW_COUNT":
                return "FOLLOW_COUNT".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // 粉丝数量相关
            case "FOLLOWER_COUNT":
                return "FOLLOWER_COUNT".equals(triggerType) && params.length > 0 &&
                       ((Long) params[0]) >= conditionValue;

            // HD上传相关
            case "HD_UPLOAD":
                return "HD_UPLOAD".equals(triggerType);

            // 快速回复相关
            case "QUICK_REPLY":
                return "QUICK_REPLY".equals(triggerType);

            // 探索所有相关
            case "EXPLORE_ALL":
                return "EXPLORE_ALL".equals(triggerType);

            default:
                System.err.println("⚠️ 未知的成就条件类型: " + conditionType);
                return false;
        }
    }

    /**
     * 切换成就显示状态
     */
    public boolean toggleAchievementDisplay(User user, Long achievementId) {
        Optional<Achievement> achievementOpt = achievementRepository.findById(achievementId);
        if (!achievementOpt.isPresent()) {
            throw new RuntimeException("成就不存在");
        }

        Achievement achievement = achievementOpt.get();
        Optional<UserAchievement> userAchievementOpt = userAchievementRepository.findByUserAndAchievement(user, achievement);
        
        if (!userAchievementOpt.isPresent()) {
            throw new RuntimeException("用户未解锁此成就");
        }

        UserAchievement userAchievement = userAchievementOpt.get();
        userAchievement.setIsDisplayed(!userAchievement.getIsDisplayed());
        userAchievementRepository.save(userAchievement);
        
        return userAchievement.getIsDisplayed();
    }

    /**
     * 获取成就分类统计
     */
    public Map<String, Object> getAchievementCategoriesWithProgress(User user) {
        Map<String, Object> result = new HashMap<>();
        
        List<Achievement> allAchievements = getAllAchievements();
        List<UserAchievement> userAchievements = user != null ? getUserAchievements(user) : List.of();
        
        Map<String, Integer> categoryTotals = new HashMap<>();
        Map<String, Integer> categoryUnlocked = new HashMap<>();
        
        // 统计各分类的总数
        for (Achievement achievement : allAchievements) {
            String category = achievement.getCategory().toString();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0) + 1);
            categoryUnlocked.putIfAbsent(category, 0);
        }
        
        // 统计各分类的解锁数
        for (UserAchievement userAchievement : userAchievements) {
            String category = userAchievement.getAchievement().getCategory().toString();
            categoryUnlocked.put(category, categoryUnlocked.getOrDefault(category, 0) + 1);
        }
        
        result.put("categoryTotals", categoryTotals);
        result.put("categoryUnlocked", categoryUnlocked);
        
        return result;
    }

    /**
     * 根据用户行为触发成就检查
     */
    public void triggerAchievementCheck(User user, String action, int count) {
        try {
            // 先同步用户统计数据，这是修复成就检测的关键
            userLevelService.syncUserStats(user);

            // 然后检查成就
            checkAndUnlockAchievements(user, action, count);
        } catch (Exception e) {
            System.err.println("❌ 成就检查失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取成就进度
     */
    public double getAchievementProgress(User user, Achievement achievement) {
        // 简化实现，实际应该根据成就类型计算具体进度
        Optional<UserAchievement> userAchievementOpt = userAchievementRepository.findByUserAndAchievement(user, achievement);
        if (userAchievementOpt.isPresent()) {
            return userAchievementOpt.get().getProgress();
        }
        return 0.0;
    }

    /**
     * 创建默认成就
     */
    @Transactional(readOnly = false)
    public void createDefaultAchievements() {
        if (achievementRepository.count() == 0) {
            // 上传相关成就
            createAchievement("首次上传", "上传第一个视频", "fa-upload",
                Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.COMMON, 10,
                "VIDEO_COUNT", 1L);

            createAchievement("初级创作者", "上传5个视频", "fa-video",
                Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.COMMON, 25,
                "VIDEO_COUNT", 5L);

            createAchievement("活跃创作者", "上传10个视频", "fa-film",
                Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.UNCOMMON, 50,
                "VIDEO_COUNT", 10L);

            createAchievement("资深创作者", "上传25个视频", "fa-camera",
                Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.RARE, 100,
                "VIDEO_COUNT", 25L);

            createAchievement("专业创作者", "上传50个视频", "fa-broadcast-tower",
                Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.EPIC, 200,
                "VIDEO_COUNT", 50L);

            createAchievement("传奇创作者", "上传100个视频", "fa-crown",
                Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.LEGENDARY, 500,
                "VIDEO_COUNT", 100L);

            createAchievement("高产作家", "上传200个视频", "fa-trophy",
                Achievement.AchievementCategory.UPLOAD, Achievement.AchievementRarity.LEGENDARY, 1000,
                "VIDEO_COUNT", 200L);

            // 社交相关成就
            createAchievement("初次点赞", "获得第一个点赞", "fa-thumbs-up",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.COMMON, 5,
                "LIKE_COUNT", 1L);

            createAchievement("受欢迎", "获得10个点赞", "fa-heart",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.COMMON, 20,
                "LIKE_COUNT", 10L);

            createAchievement("小有名气", "获得50个点赞", "fa-star",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.UNCOMMON, 40,
                "LIKE_COUNT", 50L);

            createAchievement("点赞达人", "获得100个点赞", "fa-fire",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.UNCOMMON, 75,
                "LIKE_COUNT", 100L);

            createAchievement("人气之星", "获得500个点赞", "fa-gem",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.RARE, 150,
                "LIKE_COUNT", 500L);

            createAchievement("网红达人", "获得1000个点赞", "fa-medal",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.EPIC, 300,
                "LIKE_COUNT", 1000L);

            createAchievement("超级明星", "获得5000个点赞", "fa-crown",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.LEGENDARY, 750,
                "LIKE_COUNT", 5000L);

            createAchievement("初次评论", "发表第一条评论", "fa-comment",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.COMMON, 5,
                "COMMENT_COUNT", 1L);

            createAchievement("话痨", "发表10条评论", "fa-comments",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.COMMON, 15,
                "COMMENT_COUNT", 10L);

            createAchievement("评论专家", "发表50条评论", "fa-comment-dots",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.UNCOMMON, 35,
                "COMMENT_COUNT", 50L);

            createAchievement("互动达人", "发表100条评论", "fa-handshake",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.RARE, 60,
                "COMMENT_COUNT", 100L);

            createAchievement("社区活跃分子", "发表500条评论", "fa-users",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.EPIC, 150,
                "COMMENT_COUNT", 500L);

            // 观看相关成就
            createAchievement("初次观看", "观看第一个视频", "fa-play",
                Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.COMMON, 5,
                "WATCH_TIME", 1L);

            createAchievement("电影爱好者", "观看视频超过1小时", "fa-clock",
                Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.COMMON, 10,
                "WATCH_TIME", 3600L);

            createAchievement("追剧达人", "观看视频超过10小时", "fa-tv",
                Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.UNCOMMON, 25,
                "WATCH_TIME", 36000L);

            createAchievement("观影狂人", "观看视频超过50小时", "fa-eye",
                Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.RARE, 75,
                "WATCH_TIME", 180000L);

            createAchievement("视频收藏家", "观看视频超过100小时", "fa-archive",
                Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.EPIC, 150,
                "WATCH_TIME", 360000L);

            createAchievement("终极观众", "观看视频超过500小时", "fa-infinity",
                Achievement.AchievementCategory.WATCH, Achievement.AchievementRarity.LEGENDARY, 500,
                "WATCH_TIME", 1800000L);

            // 基础成就
            createAchievement("新手上路", "完成个人资料设置", "fa-user-edit",
                Achievement.AchievementCategory.BASIC, Achievement.AchievementRarity.COMMON, 10,
                "PROFILE_COMPLETE", 1L);

            createAchievement("坚持不懈", "连续登录7天", "fa-calendar-check",
                Achievement.AchievementCategory.BASIC, Achievement.AchievementRarity.COMMON, 20,
                "CONSECUTIVE_DAYS", 7L);

            createAchievement("忠实用户", "连续登录30天", "fa-calendar-alt",
                Achievement.AchievementCategory.BASIC, Achievement.AchievementRarity.UNCOMMON, 50,
                "CONSECUTIVE_DAYS", 30L);

            createAchievement("铁杆粉丝", "连续登录100天", "fa-calendar-plus",
                Achievement.AchievementCategory.BASIC, Achievement.AchievementRarity.RARE, 150,
                "CONSECUTIVE_DAYS", 100L);

            createAchievement("超级用户", "连续登录365天", "fa-calendar-star",
                Achievement.AchievementCategory.BASIC, Achievement.AchievementRarity.LEGENDARY, 500,
                "CONSECUTIVE_DAYS", 365L);

            createAchievement("分享达人", "分享10个视频", "fa-share",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.UNCOMMON, 30,
                "SHARE_COUNT", 10L);

            createAchievement("收藏家", "收藏50个视频", "fa-bookmark",
                Achievement.AchievementCategory.SOCIAL, Achievement.AchievementRarity.RARE, 40,
                "FAVORITE_COUNT", 50L);

            // 里程碑成就
            createAchievement("新手", "达到2级", "fa-seedling",
                Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.COMMON, 15,
                "LEVEL", 2L);

            createAchievement("进阶用户", "达到5级", "fa-leaf",
                Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.COMMON, 25,
                "LEVEL", 5L);

            createAchievement("等级达人", "达到10级", "fa-star",
                Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.UNCOMMON, 50,
                "LEVEL", 10L);

            createAchievement("资深用户", "达到20级", "fa-award",
                Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.RARE, 100,
                "LEVEL", 20L);

            createAchievement("专家级别", "达到50级", "fa-trophy",
                Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.EPIC, 250,
                "LEVEL", 50L);

            createAchievement("传奇等级", "达到100级", "fa-crown",
                Achievement.AchievementCategory.MILESTONE, Achievement.AchievementRarity.LEGENDARY, 500,
                "LEVEL", 100L);

            // 特殊成就
            createAchievement("早起鸟儿", "在早上6点前上传视频", "fa-sun",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.UNCOMMON, 30,
                "EARLY_UPLOAD", 1L);

            createAchievement("夜猫子", "在深夜12点后上传视频", "fa-moon",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.UNCOMMON, 30,
                "LATE_UPLOAD", 1L);

            createAchievement("周末战士", "在周末上传视频", "fa-calendar-weekend",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.COMMON, 20,
                "WEEKEND_UPLOAD", 1L);

            createAchievement("节日庆祝", "在节假日上传视频", "fa-gift",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.RARE, 50,
                "HOLIDAY_UPLOAD", 1L);

            createAchievement("生日快乐", "在生日当天登录", "fa-birthday-cake",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.EPIC, 100,
                "BIRTHDAY_LOGIN", 1L);

            createAchievement("完美主义者", "上传的视频获得100%好评率", "fa-check-circle",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.LEGENDARY, 200,
                "PERFECT_RATING", 1L);

            createAchievement("病毒传播", "单个视频获得10000次观看", "fa-virus",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.LEGENDARY, 300,
                "VIRAL_VIDEO", 10000L);

            createAchievement("社交蝴蝶", "关注100个用户", "fa-user-friends",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.RARE, 75,
                "FOLLOW_COUNT", 100L);

            createAchievement("人气磁铁", "被1000个用户关注", "fa-magnet",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.LEGENDARY, 500,
                "FOLLOWER_COUNT", 1000L);

            createAchievement("多才多艺", "在5个不同分类上传视频", "fa-palette",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.EPIC, 150,
                "CATEGORY_DIVERSITY", 5L);

            createAchievement("技术专家", "上传高清视频", "fa-hd-video",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.UNCOMMON, 40,
                "HD_UPLOAD", 1L);

            createAchievement("速度之王", "快速回复评论", "fa-bolt",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.RARE, 60,
                "QUICK_REPLY", 10L);

            createAchievement("探索者", "观看所有分类的视频", "fa-compass",
                Achievement.AchievementCategory.SPECIAL, Achievement.AchievementRarity.EPIC, 100,
                "EXPLORE_ALL", 1L);
        }
    }

    private void createAchievement(String name, String description, String icon,
                                 Achievement.AchievementCategory category,
                                 Achievement.AchievementRarity rarity, int points,
                                 String conditionType, Long conditionValue) {
        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setIcon(icon);
        achievement.setCategory(category);
        achievement.setRarity(rarity);
        achievement.setPoints(points);
        achievement.setConditionType(conditionType);
        achievement.setConditionValue(conditionValue);
        achievement.setIsActive(true);
        achievementRepository.save(achievement);
    }

    /**
     * 获取用户可获得的成就（未解锁的成就）
     */
    public List<Achievement> getAvailableAchievements(User user) {
        List<Achievement> allAchievements = getAllAchievements();
        List<UserAchievement> userAchievements = getUserAchievements(user);

        // 获取已解锁的成就ID
        List<Long> unlockedAchievementIds = userAchievements.stream()
            .map(ua -> ua.getAchievement().getId())
            .toList();

        // 返回未解锁的成就
        return allAchievements.stream()
            .filter(achievement -> !unlockedAchievementIds.contains(achievement.getId()))
            .toList();
    }

    /**
     * 获取用户成就进度统计
     */
    public AchievementProgressInfo getAchievementProgress(User user) {
        List<Achievement> allAchievements = getAllAchievements();
        List<UserAchievement> userAchievements = getUserAchievements(user);

        int totalAchievements = allAchievements.size();
        int unlockedAchievements = userAchievements.size();
        double completionRate = totalAchievements > 0 ? (double) unlockedAchievements / totalAchievements * 100 : 0;

        int totalPoints = userAchievements.stream()
            .mapToInt(ua -> ua.getAchievement().getPoints())
            .sum();

        return new AchievementProgressInfo(
            totalAchievements,
            unlockedAchievements,
            completionRate,
            totalPoints
        );
    }

    /**
     * 成就进度信息类
     */
    public static class AchievementProgressInfo {
        private final int totalAchievements;
        private final int unlockedAchievements;
        private final double completionRate;
        private final int totalPoints;

        public AchievementProgressInfo(int totalAchievements, int unlockedAchievements,
                                     double completionRate, int totalPoints) {
            this.totalAchievements = totalAchievements;
            this.unlockedAchievements = unlockedAchievements;
            this.completionRate = completionRate;
            this.totalPoints = totalPoints;
        }

        public int getTotalAchievements() { return totalAchievements; }
        public int getUnlockedAchievements() { return unlockedAchievements; }
        public double getCompletionRate() { return completionRate; }
        public int getTotalPoints() { return totalPoints; }
    }

    // ==================== 新的成就触发系统 ====================

    /**
     * 统一的成就触发入口 - 根据用户行为触发相应成就检查
     */
    public void triggerAchievementCheck(User user, String actionType, Object... params) {
        try {
            System.out.println("🎯 触发成就检查: " + actionType + " for user: " + user.getUsername());

            switch (actionType) {
                case "REGISTER":
                    checkRegisterAchievements(user);
                    break;
                case "UPLOAD_VIDEO":
                    checkUploadVideoAchievements(user, params);
                    break;
                case "LIKE_VIDEO":
                    checkLikeVideoAchievements(user, params);
                    break;
                case "COMMENT":
                    checkCommentAchievements(user, params);
                    break;
                case "WATCH_VIDEO":
                    checkWatchVideoAchievements(user, params);
                    break;
                case "WATCH_TIME":
                    checkWatchTimeAchievements(user, params);
                    break;
                case "CONSECUTIVE_DAYS":
                    checkConsecutiveDaysAchievements(user, params);
                    break;
                case "RECEIVE_LIKE":
                    checkReceiveLikeAchievements(user, params);
                    break;
                case "FOLLOW_USER":
                    checkFollowUserAchievements(user, params);
                    break;
                case "RECEIVE_FOLLOW":
                    checkReceiveFollowAchievements(user, params);
                    break;
                default:
                    System.out.println("⚠️ 未知的成就触发类型: " + actionType);
            }
        } catch (Exception e) {
            System.err.println("❌ 成就检查失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 具体的成就检查方法 ====================

    /**
     * 检查注册相关成就
     */
    private void checkRegisterAchievements(User user) {
        // 初来乍到 (ID: 22) - 完成用户注册
        checkAndUnlockAchievements(user, "REGISTER", 1);
    }

    /**
     * 检查视频上传相关成就
     */
    private void checkUploadVideoAchievements(User user, Object... params) {
        // 获取用户总上传视频数
        long totalVideoCount = videoRepository.countByUserId(user.getId());

        // 检查上传数量成就
        checkAndUnlockAchievements(user, "UPLOAD_VIDEO", totalVideoCount);

        // 检查时间相关的上传成就
        checkUploadTimeAchievements(user);

        // 检查周末上传成就
        checkWeekendUploadAchievements(user);

        // 检查节假日上传成就
        checkHolidayUploadAchievements(user);

        // 检查分类多样性成就
        checkCategoryDiversityAchievements(user);

        // 检查视频时长相关成就
        if (params.length > 0 && params[0] instanceof Integer) {
            int videoDurationSeconds = (Integer) params[0];
            checkVideoDurationAchievements(user, videoDurationSeconds);
        }
    }

    /**
     * 检查点赞视频相关成就
     */
    private void checkLikeVideoAchievements(User user, Object... params) {
        // 获取用户总点赞数
        long totalLikesGiven = videoLikeRepository.countByUserId(user.getId());

        // 检查点赞数量成就
        checkAndUnlockAchievements(user, "LIKE_VIDEO", totalLikesGiven);
    }

    /**
     * 检查评论相关成就
     */
    private void checkCommentAchievements(User user, Object... params) {
        // 获取用户总评论数
        long totalComments = commentRepository.countByUserId(user.getId());

        // 检查评论数量成就
        checkAndUnlockAchievements(user, "COMMENT", totalComments);
    }

    /**
     * 检查观看视频相关成就
     */
    private void checkWatchVideoAchievements(User user, Object... params) {
        // 获取用户总观看视频数
        long totalWatchedVideos = viewHistoryRepository.countDistinctVideosByUserId(user.getId());

        // 检查观看数量成就
        checkAndUnlockAchievements(user, "WATCH_VIDEO", totalWatchedVideos);
    }

    /**
     * 检查观看时长相关成就
     */
    private void checkWatchTimeAchievements(User user, Object... params) {
        // 获取用户总观看时长（秒）
        long totalWatchTimeSeconds = viewHistoryRepository.sumWatchTimeByUserId(user.getId());

        // 检查观看时长成就
        checkAndUnlockAchievements(user, "WATCH_TIME", totalWatchTimeSeconds);
    }

    /**
     * 检查连续登录相关成就
     */
    private void checkConsecutiveDaysAchievements(User user, Object... params) {
        if (params.length > 0 && params[0] instanceof Integer) {
            int consecutiveDays = (Integer) params[0];

            // 检查连续登录成就
            checkAndUnlockAchievements(user, "CONSECUTIVE_DAYS", consecutiveDays);
        }
    }

    /**
     * 检查获得点赞相关成就
     */
    private void checkReceiveLikeAchievements(User user, Object... params) {
        // 获取用户视频总获得点赞数
        long totalLikesReceived = videoLikeRepository.countByVideoUserId(user.getId());

        // 检查获得点赞成就
        checkAndUnlockAchievements(user, "RECEIVE_LIKE", totalLikesReceived);

        // 检查总点赞里程碑成就
        checkAndUnlockAchievements(user, "TOTAL_LIKES", totalLikesReceived);
    }

    /**
     * 检查关注用户相关成就
     */
    private void checkFollowUserAchievements(User user, Object... params) {
        // 获取用户关注的总数
        long totalFollowing = userFollowRepository.countByFollowerId(user.getId());

        // 检查关注用户成就
        checkAndUnlockAchievements(user, "FOLLOW_USER", totalFollowing);
    }

    /**
     * 检查获得关注相关成就
     */
    private void checkReceiveFollowAchievements(User user, Object... params) {
        // 获取用户的粉丝总数
        long totalFollowers = userFollowRepository.countByFollowingId(user.getId());

        // 检查获得关注成就
        checkAndUnlockAchievements(user, "RECEIVE_FOLLOW", totalFollowers);
    }

    // ==================== 辅助检查方法 ====================

    /**
     * 检查上传时间相关成就
     */
    private void checkUploadTimeAchievements(User user) {
        java.time.LocalTime now = java.time.LocalTime.now();

        // 早起鸟 (6-8点上传)
        if (now.isAfter(java.time.LocalTime.of(6, 0)) && now.isBefore(java.time.LocalTime.of(8, 0))) {
            checkAndUnlockAchievements(user, "UPLOAD_TIME", 6);
        }

        // 夜猫子 (2-6点上传)
        if (now.isAfter(java.time.LocalTime.of(2, 0)) && now.isBefore(java.time.LocalTime.of(6, 0))) {
            checkAndUnlockAchievements(user, "UPLOAD_TIME", 2);
        }
    }

    /**
     * 检查周末上传成就
     */
    private void checkWeekendUploadAchievements(User user) {
        java.time.DayOfWeek dayOfWeek = java.time.LocalDate.now().getDayOfWeek();

        if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            // 获取用户在周末上传的视频数量
            long weekendUploads = videoRepository.countWeekendUploadsByUserId(user.getId());
            checkAndUnlockAchievements(user, "WEEKEND_UPLOAD", weekendUploads);
        }
    }

    /**
     * 检查节假日上传成就
     */
    private void checkHolidayUploadAchievements(User user) {
        java.time.LocalDate today = java.time.LocalDate.now();
        if (isHoliday(today)) {
            checkAndUnlockAchievements(user, "HOLIDAY_UPLOAD", 1);
        }
    }

    /**
     * 检查分类多样性成就
     */
    private void checkCategoryDiversityAchievements(User user) {
        // 获取用户上传视频的不同分类数量
        long uniqueCategories = videoRepository.countDistinctCategoriesByUserId(user.getId());
        checkAndUnlockAchievements(user, "CATEGORY_DIVERSITY", uniqueCategories);
    }

    /**
     * 检查视频时长相关成就
     */
    private void checkVideoDurationAchievements(User user, int videoDurationSeconds) {
        // 短视频成就 (小于5分钟)
        if (videoDurationSeconds < 300) {
            long shortVideos = videoRepository.countShortVideosByUserId(user.getId());
            checkAndUnlockAchievements(user, "SHORT_VIDEO", shortVideos);
        }

        // 长视频成就 (大于30分钟)
        if (videoDurationSeconds > 1800) {
            long longVideos = videoRepository.countLongVideosByUserId(user.getId());
            checkAndUnlockAchievements(user, "LONG_VIDEO", longVideos);
        }

        // 超长视频成就 (大于60分钟)
        if (videoDurationSeconds > 3600) {
            long marathonVideos = videoRepository.countMarathonVideosByUserId(user.getId());
            checkAndUnlockAchievements(user, "MARATHON_VIDEO", marathonVideos);
        }
    }

    /**
     * 检查是否是节假日
     */
    private boolean isHoliday(java.time.LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // 新年
        if (month == 1 && day == 1) return true;
        // 春节（简化为2月1日）
        if (month == 2 && day == 1) return true;
        // 劳动节
        if (month == 5 && day == 1) return true;
        // 国庆节
        if (month == 10 && day == 1) return true;
        // 圣诞节
        if (month == 12 && day == 25) return true;

        return false;
    }

    /**
     * 根据ID获取成就
     */
    public Optional<Achievement> getAchievementById(Long achievementId) {
        return achievementRepository.findById(achievementId);
    }

    /**
     * 检查用户是否已解锁指定成就
     */
    public boolean hasUserUnlockedAchievement(User user, Achievement achievement) {
        Optional<UserAchievement> userAchievement = userAchievementRepository.findByUserAndAchievement(user, achievement);
        return userAchievement.isPresent();
    }

    // ==================== 成就通知功能 ====================

    /**
     * 创建成就解锁通知
     */
    private void createAchievementNotification(User user, Achievement achievement) {
        try {
            notificationService.createAchievementNotification(
                user.getId(),
                achievement.getName(),
                achievement.getIcon(),
                achievement.getPoints(),
                achievement.getId()
            );

            System.out.println("🎉 成就解锁通知已发送到通知中心: " + achievement.getName());
        } catch (Exception e) {
            System.err.println("创建成就通知失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建成就解锁通知
     */
    public void createBatchAchievementNotifications(User user, List<Achievement> achievements) {
        for (Achievement achievement : achievements) {
            createAchievementNotification(user, achievement);
        }
    }

    // ==================== 成就进度显示功能 ====================

    /**
     * 获取用户成就进度信息
     */
    public Map<String, Object> getUserAchievementProgress(User user) {
        Map<String, Object> result = new HashMap<>();
        List<Achievement> allAchievements = getAllAchievements();
        List<UserAchievement> userAchievements = getUserAchievements(user);

        // 创建已解锁成就的映射
        Map<Long, UserAchievement> unlockedMap = new HashMap<>();
        for (UserAchievement ua : userAchievements) {
            unlockedMap.put(ua.getAchievement().getId(), ua);
        }

        List<Map<String, Object>> achievementList = new ArrayList<>();

        for (Achievement achievement : allAchievements) {
            Map<String, Object> achievementInfo = new HashMap<>();
            achievementInfo.put("id", achievement.getId());
            achievementInfo.put("name", achievement.getName());
            achievementInfo.put("description", achievement.getDescription());
            achievementInfo.put("icon", achievement.getIcon());
            achievementInfo.put("points", achievement.getPoints());
            achievementInfo.put("category", achievement.getCategory());
            achievementInfo.put("rarity", achievement.getRarity());

            // 检查是否已解锁
            boolean isUnlocked = unlockedMap.containsKey(achievement.getId());
            achievementInfo.put("unlocked", isUnlocked);

            if (isUnlocked) {
                UserAchievement userAchievement = unlockedMap.get(achievement.getId());
                achievementInfo.put("unlockedAt", userAchievement.getUnlockedAt());
                achievementInfo.put("progress", 100.0);
                achievementInfo.put("currentValue", achievement.getConditionValue());
                achievementInfo.put("targetValue", achievement.getConditionValue());
            } else {
                // 计算进度
                AchievementProgress progress = calculateAchievementProgress(user, achievement);
                achievementInfo.put("progress", progress.getProgressPercentage());
                achievementInfo.put("currentValue", progress.getCurrentValue());
                achievementInfo.put("targetValue", progress.getTargetValue());
                achievementInfo.put("progressDescription", progress.getDescription());
            }

            achievementList.add(achievementInfo);
        }

        result.put("achievements", achievementList);
        result.put("totalAchievements", allAchievements.size());
        result.put("unlockedAchievements", userAchievements.size());
        result.put("completionRate", allAchievements.size() > 0 ?
            (double) userAchievements.size() / allAchievements.size() * 100 : 0);

        return result;
    }

    /**
     * 计算单个成就的进度
     */
    public AchievementProgress calculateAchievementProgress(User user, Achievement achievement) {
        String conditionType = achievement.getConditionType();
        Long targetValue = achievement.getConditionValue();
        long currentValue = 0;
        String description = "";

        try {
            switch (conditionType) {
                case "REGISTER":
                    currentValue = 1; // 注册后就是1
                    description = "完成注册";
                    break;

                case "UPLOAD_VIDEO":
                    currentValue = videoRepository.countByUserId(user.getId());
                    description = String.format("已上传 %d/%d 个视频", currentValue, targetValue);
                    break;

                case "LIKE_VIDEO":
                    currentValue = videoLikeRepository.countByUserId(user.getId());
                    description = String.format("已点赞 %d/%d 个视频", currentValue, targetValue);
                    break;

                case "COMMENT":
                    currentValue = commentRepository.countByUserId(user.getId());
                    description = String.format("已发表 %d/%d 条评论", currentValue, targetValue);
                    break;

                case "WATCH_VIDEO":
                    currentValue = viewHistoryRepository.countDistinctVideosByUserId(user.getId());
                    description = String.format("已观看 %d/%d 个视频", currentValue, targetValue);
                    break;

                case "WATCH_TIME":
                    currentValue = viewHistoryRepository.sumWatchTimeByUserId(user.getId());
                    long targetHours = targetValue / 3600;
                    long currentHours = currentValue / 3600;
                    description = String.format("已观看 %d/%d 小时", currentHours, targetHours);
                    break;

                case "RECEIVE_LIKE":
                    currentValue = videoLikeRepository.countByVideoUserId(user.getId());
                    description = String.format("已获得 %d/%d 个点赞", currentValue, targetValue);
                    break;

                case "FOLLOW_USER":
                    currentValue = userFollowRepository.countByFollowerId(user.getId());
                    description = String.format("已关注 %d/%d 个用户", currentValue, targetValue);
                    break;

                case "RECEIVE_FOLLOW":
                    currentValue = userFollowRepository.countByFollowingId(user.getId());
                    description = String.format("已获得 %d/%d 个关注", currentValue, targetValue);
                    break;

                case "CATEGORY_DIVERSITY":
                    currentValue = videoRepository.countDistinctCategoriesByUserId(user.getId());
                    description = String.format("已涉及 %d/%d 个分类", currentValue, targetValue);
                    break;

                case "TOTAL_LIKES":
                    currentValue = videoRepository.getTotalLikesByUserId(user.getId());
                    description = String.format("总计获得 %d/%d 个点赞", currentValue, targetValue);
                    break;

                default:
                    description = "进度计算中...";
                    break;
            }
        } catch (Exception e) {
            System.err.println("计算成就进度失败: " + e.getMessage());
            description = "进度计算失败";
        }

        double progressPercentage = targetValue > 0 ?
            Math.min(100.0, (double) currentValue / targetValue * 100) : 0;

        return new AchievementProgress(currentValue, targetValue, progressPercentage, description);
    }

    /**
     * 成就进度数据类
     */
    public static class AchievementProgress {
        private final long currentValue;
        private final long targetValue;
        private final double progressPercentage;
        private final String description;

        public AchievementProgress(long currentValue, long targetValue, double progressPercentage, String description) {
            this.currentValue = currentValue;
            this.targetValue = targetValue;
            this.progressPercentage = progressPercentage;
            this.description = description;
        }

        public long getCurrentValue() { return currentValue; }
        public long getTargetValue() { return targetValue; }
        public double getProgressPercentage() { return progressPercentage; }
        public String getDescription() { return description; }
    }

    // ==================== 成就排行榜功能 ====================

    /**
     * 获取成就排行榜
     */
    public Map<String, Object> getAchievementLeaderboard(int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取所有用户的成就统计
            List<Map<String, Object>> leaderboard = getUserAchievementStats(page, size);

            result.put("success", true);
            result.put("leaderboard", leaderboard);
            result.put("page", page);
            result.put("size", size);

        } catch (Exception e) {
            System.err.println("获取成就排行榜失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取排行榜失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户成就统计排行
     */
    private List<Map<String, Object>> getUserAchievementStats(int page, int size) {
        List<Map<String, Object>> stats = new ArrayList<>();

        // 使用原生查询获取用户成就统计
        List<Object[]> results = userAchievementRepository.getUserAchievementStats(
            PageRequest.of(page, size));

        int rank = page * size + 1;
        for (Object[] row : results) {
            Map<String, Object> userStat = new HashMap<>();
            userStat.put("rank", rank++);
            userStat.put("userId", row[0]);
            userStat.put("username", row[1]);
            userStat.put("achievementCount", row[2]);
            userStat.put("totalPoints", row[3]);
            userStat.put("completionRate", row[4]);
            userStat.put("latestAchievement", row[5]);
            userStat.put("latestAchievementTime", row[6]);

            stats.add(userStat);
        }

        return stats;
    }

    /**
     * 获取分类排行榜
     */
    public Map<String, Object> getCategoryLeaderboard(String category, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Object[]> results = userAchievementRepository.getCategoryAchievementStats(
                category, PageRequest.of(page, size));

            List<Map<String, Object>> leaderboard = new ArrayList<>();
            int rank = page * size + 1;

            for (Object[] row : results) {
                Map<String, Object> userStat = new HashMap<>();
                userStat.put("rank", rank++);
                userStat.put("userId", row[0]);
                userStat.put("username", row[1]);
                userStat.put("categoryAchievements", row[2]);
                userStat.put("categoryPoints", row[3]);

                leaderboard.add(userStat);
            }

            result.put("success", true);
            result.put("category", category);
            result.put("leaderboard", leaderboard);
            result.put("page", page);
            result.put("size", size);

        } catch (Exception e) {
            System.err.println("获取分类排行榜失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取分类排行榜失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取最近解锁成就排行
     */
    public Map<String, Object> getRecentAchievementsLeaderboard(int days, int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Object[]> results = userAchievementRepository.getRecentAchievements(days,
                PageRequest.of(0, limit));

            List<Map<String, Object>> recentAchievements = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> achievement = new HashMap<>();
                achievement.put("userId", row[0]);
                achievement.put("username", row[1]);
                achievement.put("achievementId", row[2]);
                achievement.put("achievementName", row[3]);
                achievement.put("achievementIcon", row[4]);
                achievement.put("achievementPoints", row[5]);
                achievement.put("unlockedAt", row[6]);

                recentAchievements.add(achievement);
            }

            result.put("success", true);
            result.put("recentAchievements", recentAchievements);
            result.put("days", days);
            result.put("limit", limit);

        } catch (Exception e) {
            System.err.println("获取最近成就失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取最近成就失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户在排行榜中的位置
     */
    public Map<String, Object> getUserRanking(User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取用户成就统计
            AchievementProgressInfo userStats = getAchievementProgress(user);

            // 获取用户排名
            Integer rank = userAchievementRepository.getUserRank(user.getId());

            result.put("success", true);
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("rank", rank != null ? rank : 0);
            result.put("achievementCount", userStats.getUnlockedAchievements());
            result.put("totalPoints", userStats.getTotalPoints());
            result.put("completionRate", userStats.getCompletionRate());

        } catch (Exception e) {
            System.err.println("获取用户排名失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取用户排名失败: " + e.getMessage());
        }

        return result;
    }
}
