package org.example.service;

import org.example.entity.DailyCheckin;
import org.example.entity.DailyTask;
import org.example.entity.UserTaskProgress;
import org.example.entity.User;
import org.example.repository.DailyCheckinRepository;
import org.example.repository.DailyTaskRepository;
import org.example.repository.UserTaskProgressRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class LevelService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyTaskRepository dailyTaskRepository;

    @Autowired
    private UserTaskProgressRepository userTaskProgressRepository;

    @Autowired
    private DailyCheckinRepository dailyCheckinRepository;

    // 等级配置
    private static final int MAX_LEVEL = 100;
    private static final int BASE_EXP = 100;
    private static final double EXP_MULTIPLIER = 1.2;

    // 等级称号
    private static final Map<Integer, String> LEVEL_TITLES = new HashMap<>();
    static {
        LEVEL_TITLES.put(1, "新手探索者");
        LEVEL_TITLES.put(10, "初级创作者");
        LEVEL_TITLES.put(20, "活跃用户");
        LEVEL_TITLES.put(30, "资深玩家");
        LEVEL_TITLES.put(40, "内容专家");
        LEVEL_TITLES.put(50, "社区达人");
        LEVEL_TITLES.put(60, "影响力用户");
        LEVEL_TITLES.put(70, "平台精英");
        LEVEL_TITLES.put(80, "传奇创作者");
        LEVEL_TITLES.put(90, "超级大神");
        LEVEL_TITLES.put(100, "至尊王者");
    }

    /**
     * 计算等级所需的总经验值
     */
    public long calculateExpForLevel(int level) {
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
    public int calculateLevelFromExp(long experience) {
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
    public String getLevelTitle(int level) {
        Integer[] titleLevels = LEVEL_TITLES.keySet().toArray(new Integer[0]);
        Arrays.sort(titleLevels, Collections.reverseOrder());
        
        for (Integer titleLevel : titleLevels) {
            if (level >= titleLevel) {
                return LEVEL_TITLES.get(titleLevel);
            }
        }
        return LEVEL_TITLES.get(1);
    }

    /**
     * 每日签到
     */
    @Transactional
    public Map<String, Object> dailyCheckin(Long userId) {
        LocalDate today = LocalDate.now();
        
        // 检查今日是否已签到
        Optional<DailyCheckin> todayCheckin = dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today);
        if (todayCheckin.isPresent()) {
            throw new RuntimeException("今日已签到");
        }

        // 获取最近的签到记录
        Optional<DailyCheckin> lastCheckin = dailyCheckinRepository.findTopByUserIdOrderByCheckinDateDesc(userId);
        
        int consecutiveDays = 1;
        if (lastCheckin.isPresent()) {
            LocalDate lastDate = lastCheckin.get().getCheckinDate();
            if (lastDate.equals(today.minusDays(1))) {
                // 连续签到
                consecutiveDays = lastCheckin.get().getConsecutiveDays() + 1;
            }
        }

        // 计算签到奖励
        int baseExp = 10;
        int bonusExp = Math.min(consecutiveDays / 7, 5) * 5; // 每7天连续签到额外奖励5经验，最多25
        int totalExp = baseExp + bonusExp;

        // 保存签到记录
        DailyCheckin checkin = new DailyCheckin(userId, totalExp, consecutiveDays);
        dailyCheckinRepository.save(checkin);

        // 增加用户经验
        addExperience(userId, totalExp);

        Map<String, Object> result = new HashMap<>();
        result.put("expGained", totalExp);
        result.put("consecutiveDays", consecutiveDays);
        result.put("bonusExp", bonusExp);
        
        return result;
    }

    /**
     * 获取签到状态
     */
    public Map<String, Object> getCheckinStatus(Long userId) {
        LocalDate today = LocalDate.now();
        
        Optional<DailyCheckin> todayCheckin = dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today);
        Optional<DailyCheckin> lastCheckin = dailyCheckinRepository.findTopByUserIdOrderByCheckinDateDesc(userId);
        
        int consecutiveDays = 0;
        if (lastCheckin.isPresent()) {
            if (todayCheckin.isPresent()) {
                consecutiveDays = todayCheckin.get().getConsecutiveDays();
            } else {
                LocalDate lastDate = lastCheckin.get().getCheckinDate();
                if (lastDate.equals(today.minusDays(1))) {
                    consecutiveDays = lastCheckin.get().getConsecutiveDays();
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("checkedInToday", todayCheckin.isPresent());
        result.put("consecutiveDays", consecutiveDays);
        
        return result;
    }

    /**
     * 获取每日任务列表
     */
    public List<Map<String, Object>> getDailyTasks(Long userId) {
        LocalDate today = LocalDate.now();
        List<DailyTask> allTasks = dailyTaskRepository.findByActiveTrue();
        List<Map<String, Object>> result = new ArrayList<>();

        for (DailyTask task : allTasks) {
            Optional<UserTaskProgress> progress = userTaskProgressRepository
                .findByUserIdAndTaskIdAndDate(userId, task.getId(), today);

            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("id", task.getId());
            taskInfo.put("name", task.getName());
            taskInfo.put("description", task.getDescription());
            taskInfo.put("type", task.getType());
            taskInfo.put("target", task.getTarget());
            taskInfo.put("expReward", task.getExpReward());
            
            if (progress.isPresent()) {
                taskInfo.put("progress", progress.get().getProgress());
                taskInfo.put("completed", progress.get().getCompleted());
            } else {
                taskInfo.put("progress", 0);
                taskInfo.put("completed", false);
            }
            
            result.add(taskInfo);
        }

        return result;
    }

    /**
     * 更新任务进度
     */
    @Transactional
    public void updateTaskProgress(Long userId, String taskType, int increment) {
        LocalDate today = LocalDate.now();
        List<DailyTask> tasks = dailyTaskRepository.findByTypeAndActiveTrue(taskType);

        for (DailyTask task : tasks) {
            Optional<UserTaskProgress> progressOpt = userTaskProgressRepository
                .findByUserIdAndTaskIdAndDate(userId, task.getId(), today);

            UserTaskProgress progress;
            if (progressOpt.isPresent()) {
                progress = progressOpt.get();
            } else {
                progress = new UserTaskProgress(userId, task.getId());
            }

            if (!progress.getCompleted()) {
                progress.setProgress(progress.getProgress() + increment);
                
                if (progress.getProgress() >= task.getTarget()) {
                    progress.setProgress(task.getTarget());
                    progress.setCompleted(true);
                    
                    // 奖励经验
                    addExperience(userId, task.getExpReward());
                }
                
                userTaskProgressRepository.save(progress);
            }
        }
    }

    /**
     * 初始化默认每日任务
     */
    @Transactional(readOnly = false)
    public void initializeDefaultTasks() {
        if (dailyTaskRepository.count() == 0) {
            List<DailyTask> defaultTasks = Arrays.asList(
                new DailyTask("上传视频", "今日上传1个视频", "video_upload", 1, 50),
                new DailyTask("观看视频", "观看3个视频", "video_watch", 3, 20),
                new DailyTask("发表评论", "发表5条评论", "comment", 5, 30),
                new DailyTask("点赞互动", "点赞10个视频", "like", 10, 25),
                new DailyTask("分享内容", "分享2个视频", "share", 2, 35)
            );
            
            dailyTaskRepository.saveAll(defaultTasks);
            System.out.println("✅ 初始化默认每日任务完成");
        } else {
            System.out.println("ℹ️ 每日任务已存在，跳过初始化");
        }
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
}
