package org.example.service;

import org.example.entity.User;
import org.example.entity.UserLevel;
import org.example.repository.UserLevelRepository;
import org.example.repository.UserRepository;
import org.example.repository.VideoRepository;
import org.example.repository.VideoLikeRepository;
import org.example.repository.CommentRepository;
import org.example.repository.ViewHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户等级服务
 */
@Service
@Transactional(readOnly = false)
public class UserLevelService {

    private static final Logger logger = LoggerFactory.getLogger(UserLevelService.class);

    @Autowired
    private UserLevelRepository userLevelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ViewHistoryRepository viewHistoryRepository;

    /**
     * 获取用户等级信息
     */
    public UserLevel getUserLevel(User user) {
        Optional<UserLevel> userLevelOpt = userLevelRepository.findByUser(user);
        if (userLevelOpt.isPresent()) {
            return userLevelOpt.get();
        } else {
            // 如果用户没有等级记录，创建一个新的
            return createUserLevel(user);
        }
    }

    /**
     * 创建用户等级记录
     */
    private UserLevel createUserLevel(User user) {
        UserLevel userLevel = new UserLevel(user);
        return userLevelRepository.save(userLevel);
    }

    /**
     * 增加用户经验值
     */
    public boolean addExperience(User user, Long exp) {
        return addExperience(user, exp, "系统奖励");
    }

    /**
     * 增加用户经验值（带原因）
     */
    public boolean addExperience(User user, Long exp, String reason) {
        UserLevel userLevel = getUserLevel(user);
        Integer oldLevel = userLevel.getLevel();
        boolean leveledUp = userLevel.addExperience(exp);
        userLevelRepository.save(userLevel);

        if (leveledUp) {
            // 记录升级事件
            logger.info("用户 {} 从 {} 级升级到 {} 级，原因：{}",
                    user.getId(), oldLevel, userLevel.getLevel(), reason);
        }

        return leveledUp;
    }

    /**
     * 增加用户经验值（通过用户ID）
     */
    public boolean addExperience(Long userId, Long exp, String reason) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return addExperience(userOpt.get(), exp, reason);
        }
        return false;
    }

    /**
     * 增加上传视频数量
     */
    public void incrementVideosUploaded(User user) {
        UserLevel userLevel = getUserLevel(user);
        userLevel.incrementVideosUploaded();
        userLevelRepository.save(userLevel);
    }

    /**
     * 增加获得点赞数量
     */
    public void incrementLikesReceived(User user, int count) {
        UserLevel userLevel = getUserLevel(user);
        userLevel.setTotalLikesReceived(userLevel.getTotalLikesReceived() + count);
        userLevel.setUpdatedAt(LocalDateTime.now());
        userLevelRepository.save(userLevel);
    }

    /**
     * 增加评论数量
     */
    public void incrementCommentsMade(User user) {
        UserLevel userLevel = getUserLevel(user);
        userLevel.setTotalCommentsMade(userLevel.getTotalCommentsMade() + 1);
        userLevel.setUpdatedAt(LocalDateTime.now());
        userLevelRepository.save(userLevel);
    }

    /**
     * 增加观看时长
     */
    public void addWatchTime(User user, Long seconds) {
        UserLevel userLevel = getUserLevel(user);
        userLevel.setTotalWatchTime(userLevel.getTotalWatchTime() + seconds);
        userLevel.setUpdatedAt(LocalDateTime.now());
        userLevelRepository.save(userLevel);
    }

    /**
     * 更新连续登录天数
     */
    public void updateConsecutiveDays(User user, int days) {
        UserLevel userLevel = getUserLevel(user);
        userLevel.setConsecutiveDays(days);
        userLevel.setUpdatedAt(LocalDateTime.now());
        userLevelRepository.save(userLevel);
    }

    /**
     * 获取等级排行榜
     */
    public List<UserLevel> getLevelLeaderboard(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userLevelRepository.findTopByOrderByLevelDescExperiencePointsDesc(pageable);
    }

    /**
     * 获取经验值历史记录（模拟数据，实际应该有专门的经验值记录表）
     */
    public List<Object> getExpHistory(User user, int page, int size) {
        // 这里返回模拟数据，实际应该查询经验值获取记录表
        return List.of(
            new Object[]{"上传视频", 50, LocalDateTime.now().minusDays(1)},
            new Object[]{"获得点赞", 10, LocalDateTime.now().minusDays(2)},
            new Object[]{"发表评论", 5, LocalDateTime.now().minusDays(3)}
        );
    }

    /**
     * 根据行为类型给用户增加经验值
     */
    public void addExperienceForAction(User user, String action) {
        Long exp = 0L;
        switch (action) {
            case "upload_video":
                exp = 50L;
                break;
            case "like_received":
                exp = 10L;
                break;
            case "comment_made":
                exp = 5L;
                break;
            case "daily_login":
                exp = 2L;
                break;
            case "video_watched":
                exp = 1L;
                break;
            default:
                exp = 1L;
        }
        
        if (exp > 0) {
            addExperience(user, exp);
        }
    }

    /**
     * 检查用户是否可以升级
     */
    public boolean checkLevelUp(User user) {
        UserLevel userLevel = getUserLevel(user);
        return userLevel.checkLevelUp();
    }

    /**
     * 获取用户当前等级进度
     */
    public double getLevelProgress(User user) {
        UserLevel userLevel = getUserLevel(user);
        return userLevel.getLevelProgress();
    }

    /**
     * 获取用户等级称号
     */
    public String getLevelTitle(User user) {
        UserLevel userLevel = getUserLevel(user);
        return userLevel.getLevelTitle();
    }

    /**
     * 获取用户等级进度信息
     */
    public LevelProgressInfo getLevelProgressInfo(User user) {
        UserLevel userLevel = getUserLevel(user);

        // 计算当前等级所需经验和下一等级所需经验
        long currentLevelExp = calculateLevelRequiredExp(userLevel.getLevel());
        long nextLevelExp = calculateLevelRequiredExp(userLevel.getLevel() + 1);
        long currentExp = userLevel.getExperiencePoints();

        // 计算进度百分比
        double progress = 0.0;
        boolean isMaxLevel = userLevel.getLevel() >= 10;

        if (!isMaxLevel) {
            long expInCurrentLevel = currentExp - currentLevelExp;
            long expNeededForNextLevel = nextLevelExp - currentLevelExp;
            progress = (double) expInCurrentLevel / expNeededForNextLevel * 100;
        } else {
            progress = 100.0;
        }

        return new LevelProgressInfo(currentExp, nextLevelExp, progress, isMaxLevel);
    }

    /**
     * 获取下一等级要求
     */
    public NextLevelRequirements getNextLevelRequirements(User user) {
        UserLevel userLevel = getUserLevel(user);

        if (userLevel.getLevel() >= 10) {
            return null; // 已达到最高等级
        }

        int nextLevel = userLevel.getLevel() + 1;
        long requiredExp = calculateLevelRequiredExp(nextLevel);
        long currentExp = userLevel.getExperiencePoints();
        long expNeeded = requiredExp - currentExp;

        return new NextLevelRequirements(nextLevel, requiredExp, expNeeded);
    }

    /**
     * 计算指定等级所需的经验值
     */
    private long calculateLevelRequiredExp(int level) {
        // 等级经验值计算公式
        switch (level) {
            case 1: return 0L;
            case 2: return 500L;
            case 3: return 1500L;
            case 4: return 3000L;
            case 5: return 6000L;
            case 6: return 10000L;
            case 7: return 20000L;
            case 8: return 50000L;
            case 9: return 100000L;
            case 10: return 200000L;
            default: return 200000L; // 最高等级
        }
    }

    /**
     * 等级进度信息类
     */
    public static class LevelProgressInfo {
        private final long currentExp;
        private final long nextLevelExp;
        private final double progress;
        private final boolean isMaxLevel;

        public LevelProgressInfo(long currentExp, long nextLevelExp, double progress, boolean isMaxLevel) {
            this.currentExp = currentExp;
            this.nextLevelExp = nextLevelExp;
            this.progress = progress;
            this.isMaxLevel = isMaxLevel;
        }

        public long getCurrentExp() { return currentExp; }
        public long getNextLevelExp() { return nextLevelExp; }
        public double getProgress() { return progress; }
        public boolean isMaxLevel() { return isMaxLevel; }
    }

    /**
     * 下一等级要求信息类
     */
    public static class NextLevelRequirements {
        private final int nextLevel;
        private final long requiredExp;
        private final long expNeeded;

        public NextLevelRequirements(int nextLevel, long requiredExp, long expNeeded) {
            this.nextLevel = nextLevel;
            this.requiredExp = requiredExp;
            this.expNeeded = expNeeded;
        }

        public int getNextLevel() { return nextLevel; }
        public long getRequiredExp() { return requiredExp; }
        public long getExpNeeded() { return expNeeded; }
    }

    /**
     * 同步用户统计数据到UserLevel表
     * 这是修复成就检测的关键方法
     */
    public void syncUserStats(User user) {
        try {
            UserLevel userLevel = getUserLevel(user);

            // 同步视频上传数量
            long totalVideos = videoRepository.countByUserId(user.getId());
            userLevel.setTotalVideosUploaded((int) totalVideos);

            // 同步收到的点赞数
            long totalLikesReceived = videoLikeRepository.countByVideoUserId(user.getId());
            userLevel.setTotalLikesReceived((int) totalLikesReceived);

            // 同步评论数
            long totalComments = commentRepository.countByUserId(user.getId());
            userLevel.setTotalCommentsMade((int) totalComments);

            // 同步观看时长
            long totalWatchTime = viewHistoryRepository.sumWatchTimeByUserId(user.getId());
            userLevel.setTotalWatchTime(totalWatchTime);

            // 同步连续签到天数（从User表同步到UserLevel表）
            Integer consecutiveCheckinDays = user.getConsecutiveCheckinDays();
            if (consecutiveCheckinDays != null) {
                userLevel.setConsecutiveDays(consecutiveCheckinDays);
            }

            userLevel.setUpdatedAt(LocalDateTime.now());
            userLevelRepository.save(userLevel);

            logger.info("✅ 用户 {} 的统计数据已同步: 视频{}个, 收到点赞{}个, 评论{}个",
                user.getUsername(), totalVideos, totalLikesReceived, totalComments);

        } catch (Exception e) {
            logger.error("❌ 同步用户统计数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 批量同步所有用户的统计数据
     */
    public void syncAllUsersStats() {
        try {
            List<User> allUsers = userRepository.findAll();
            logger.info("开始同步 {} 个用户的统计数据", allUsers.size());

            for (User user : allUsers) {
                syncUserStats(user);
            }

            logger.info("✅ 所有用户统计数据同步完成");
        } catch (Exception e) {
            logger.error("❌ 批量同步用户统计数据失败: {}", e.getMessage(), e);
        }
    }
}
