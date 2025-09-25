package org.example.service;

import org.example.entity.User;
import org.example.entity.Video;
import org.example.entity.ViewHistory;
import org.example.repository.ViewHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class ViewHistoryService {

    @Autowired
    private ViewHistoryRepository viewHistoryRepository;
    
    @Autowired
    private AchievementService achievementService;

    /**
     * 记录用户观看视频
     */
    public ViewHistory recordView(User user, Video video, int watchTimeSeconds) {
        // 检查是否已有观看记录
        Optional<ViewHistory> existingView = viewHistoryRepository.findByUserAndVideo(user, video);

        ViewHistory viewHistory;
        if (existingView.isPresent()) {
            // 更新现有记录
            viewHistory = existingView.get();
            viewHistory.setWatchDuration(Math.max(viewHistory.getWatchDuration(), (long) watchTimeSeconds));
            viewHistory.setUpdatedAt(LocalDateTime.now());
        } else {
            // 创建新记录
            viewHistory = new ViewHistory(user, video);
            viewHistory.setWatchDuration((long) watchTimeSeconds);
        }
        
        ViewHistory savedView = viewHistoryRepository.save(viewHistory);
        
        // 触发观看相关成就检查
        try {
            achievementService.triggerAchievementCheck(user, "WATCH_VIDEO", 1);
            achievementService.triggerAchievementCheck(user, "WATCH_TIME", watchTimeSeconds);
        } catch (Exception e) {
            System.err.println("❌ 观看成就检查失败: " + e.getMessage());
        }
        
        return savedView;
    }

    /**
     * 获取用户观看历史
     */
    public List<ViewHistory> getUserViewHistory(User user, int limit) {
        return viewHistoryRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * 获取用户观看的视频总数
     */
    public long getUserWatchedVideoCount(User user) {
        return viewHistoryRepository.countDistinctVideosByUserId(user.getId());
    }

    /**
     * 获取用户总观看时长（秒）
     */
    public long getUserTotalWatchTime(User user) {
        return viewHistoryRepository.sumWatchTimeByUserId(user.getId());
    }

    /**
     * 检查用户是否观看过某个视频
     */
    public boolean hasUserWatchedVideo(User user, Video video) {
        return viewHistoryRepository.findByUserAndVideo(user, video).isPresent();
    }

    /**
     * 获取视频的观看次数
     */
    public long getVideoViewCount(Video video) {
        Object[] stats = viewHistoryRepository.getVideoViewingStats(video);
        return stats != null && stats[0] != null ? (Long) stats[0] : 0L;
    }

    /**
     * 删除用户的观看历史
     */
    public void clearUserViewHistory(User user) {
        viewHistoryRepository.deleteByUser(user);
    }

    /**
     * 删除特定的观看记录
     */
    public void deleteViewHistory(User user, Video video) {
        viewHistoryRepository.findByUserAndVideo(user, video)
                .ifPresent(viewHistoryRepository::delete);
    }
}
