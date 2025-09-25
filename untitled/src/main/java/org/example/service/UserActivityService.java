package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.repository.VideoRepository;
import org.example.repository.CommentRepository;
import org.example.repository.VideoLikeRepository;
import org.example.repository.DailyCheckinRepository;
import org.example.repository.UserTaskProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class UserActivityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private DailyCheckinRepository dailyCheckinRepository;

    @Autowired
    private UserTaskProgressRepository userTaskProgressRepository;

    @Autowired
    private LevelService levelService;

    // 等级配置
    private static final int MAX_LEVEL = 100;
    private static final int BASE_EXP = 100;
    private static final double EXP_MULTIPLIER = 1.2;

    /**
     * 根据用户名获取用户ID
     */
    public Long getUserIdByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(User::getId).orElse(null);
    }

    /**
     * 获取用户等级信息
     */
    public Map<String, Object> getUserLevelInfo(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();
        long experience = user.getExperience() != null ? user.getExperience() : 0L;
        
        int level = calculateLevelFromExp(experience);
        String title = getLevelTitle(level);
        
        // 计算当前等级和下一等级的经验
        long currentLevelExp = calculateExpForLevel(level);
        long nextLevelExp = level >= MAX_LEVEL ? currentLevelExp : calculateExpForLevel(level + 1);
        long expInCurrentLevel = experience - currentLevelExp;
        long expNeededForNext = nextLevelExp - currentLevelExp;
        
        double progress = expNeededForNext > 0 ? (double) expInCurrentLevel / expNeededForNext * 100 : 100;

        Map<String, Object> result = new HashMap<>();
        result.put("level", level);
        result.put("title", title);
        result.put("experience", experience);
        result.put("currentLevelExp", currentLevelExp);
        result.put("nextLevelExp", nextLevelExp);
        result.put("expInCurrentLevel", expInCurrentLevel);
        result.put("expNeededForNext", expNeededForNext);
        result.put("progress", Math.round(progress * 100.0) / 100.0);
        result.put("isMaxLevel", level >= MAX_LEVEL);
        
        // 添加统计数据
        result.put("achievementCount", 0); // TODO: 实现成就系统
        result.put("consecutiveCheckins", getConsecutiveCheckins(userId));
        result.put("completedTasks", userTaskProgressRepository.countCompletedTasksByUserId(userId));
        
        return result;
    }

    /**
     * 获取用户活动统计
     */
    public Map<String, Object> getUserActivityStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        stats.put("totalVideos", videoRepository.countByUserId(userId));
        stats.put("totalComments", commentRepository.countByUserId(userId));
        stats.put("totalLikes", videoLikeRepository.countByUserId(userId));
        stats.put("totalCheckins", dailyCheckinRepository.countByUserId(userId));
        stats.put("completedTasks", userTaskProgressRepository.countCompletedTasksByUserId(userId));
        
        return stats;
    }

    /**
     * 增加用户经验值
     */
    @Transactional
    public void addExperience(Long userId, int exp) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            long currentExp = user.getExperience() != null ? user.getExperience() : 0L;
            user.setExperience(currentExp + exp);
            userRepository.save(user);
        }
    }

    /**
     * 计算等级所需的总经验值
     */
    private long calculateExpForLevel(int level) {
        if (level <= 1) return 0;
        long totalExp = 0;
        for (int i = 1; i < level; i++) {
            totalExp += (long) Math.floor(BASE_EXP * Math.pow(EXP_MULTIPLIER, i - 1));
        }
        return totalExp;
    }

    /**
     * 根据经验值计算等级
     */
    private int calculateLevelFromExp(long experience) {
        for (int level = 1; level <= MAX_LEVEL; level++) {
            if (experience < calculateExpForLevel(level + 1)) {
                return level;
            }
        }
        return MAX_LEVEL;
    }

    /**
     * 获取等级称号
     */
    private String getLevelTitle(int level) {
        if (level >= 100) return "至尊王者";
        if (level >= 90) return "超级大神";
        if (level >= 80) return "传奇创作者";
        if (level >= 70) return "平台精英";
        if (level >= 60) return "影响力用户";
        if (level >= 50) return "社区达人";
        if (level >= 40) return "内容专家";
        if (level >= 30) return "资深玩家";
        if (level >= 20) return "活跃用户";
        if (level >= 10) return "初级创作者";
        return "新手探索者";
    }

    /**
     * 获取连续签到天数
     */
    private int getConsecutiveCheckins(Long userId) {
        return dailyCheckinRepository.findTopByUserIdOrderByCheckinDateDesc(userId)
            .map(checkin -> checkin.getConsecutiveDays())
            .orElse(0);
    }
}
