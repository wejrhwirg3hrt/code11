package org.example.service;

import org.example.entity.User;
import org.example.entity.Video;
import org.example.entity.ViewHistory;
import org.example.repository.ViewHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐算法服务
 */
@Service
@Transactional(readOnly = false)
public class RecommendationService {

    @Autowired
    private ViewHistoryRepository viewHistoryRepository;

    @Autowired
    private VideoService videoService;

    @Autowired
    private TagService tagService;

    /**
     * 记录用户观看历史
     */
    public void recordViewHistory(User user, Video video, Long watchDuration, Long lastPosition, HttpServletRequest request) {
        if (user == null || video == null) {
            return;
        }

        Optional<ViewHistory> existingHistory = viewHistoryRepository.findByUserAndVideo(user, video);
        
        if (existingHistory.isPresent()) {
            // 更新现有记录
            ViewHistory history = existingHistory.get();
            history.incrementWatchCount();
            history.updateProgress(watchDuration, lastPosition);
            
            if (request != null) {
                history.setIpAddress(getClientIpAddress(request));
                history.setUserAgent(request.getHeader("User-Agent"));
                history.setDeviceType(detectDeviceType(request.getHeader("User-Agent")));
            }
            
            viewHistoryRepository.save(history);
        } else {
            // 创建新记录
            ViewHistory history = new ViewHistory(user, video);
            history.setWatchDuration(watchDuration);
            history.setLastPosition(lastPosition);
            
            if (video.getDuration() != null && !video.getDuration().trim().isEmpty()) {
                try {
                    Long videoDurationSeconds = parseDurationToSeconds(video.getDuration());
                    if (videoDurationSeconds > 0) {
                        history.setCompletionRate((double) watchDuration / videoDurationSeconds);
                    }
                } catch (Exception e) {
                    // 如果解析失败，设置默认完成率
                    history.setCompletionRate(0.0);
                }
            }
            
            if (request != null) {
                history.setIpAddress(getClientIpAddress(request));
                history.setUserAgent(request.getHeader("User-Agent"));
                history.setDeviceType(detectDeviceType(request.getHeader("User-Agent")));
            }
            
            viewHistoryRepository.save(history);
        }
    }

    /**
     * 获取个性化推荐视频
     */
    public List<Video> getPersonalizedRecommendations(User user, int limit) {
        if (user == null) {
            return getPopularVideos(limit);
        }

        List<Video> recommendations = new ArrayList<>();
        
        // 1. 基于协同过滤的推荐 (30%)
        List<Video> collaborativeRecommendations = getCollaborativeFilteringRecommendations(user, limit / 3);
        recommendations.addAll(collaborativeRecommendations);
        
        // 2. 基于内容的推荐 (40%)
        List<Video> contentBasedRecommendations = getContentBasedRecommendations(user, limit * 2 / 5);
        recommendations.addAll(contentBasedRecommendations);
        
        // 3. 热门视频推荐 (30%)
        List<Video> popularRecommendations = getPopularVideos(limit / 3);
        recommendations.addAll(popularRecommendations);
        
        // 去重并限制数量
        return recommendations.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 基于协同过滤的推荐
     */
    public List<Video> getCollaborativeFilteringRecommendations(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit * 2); // 获取更多候选，然后筛选
        
        List<Object[]> results = viewHistoryRepository.findCollaborativeFilteringRecommendations(user, pageable);
        
        return results.stream()
                .map(result -> (Video) result[0])
                .filter(video -> video.getStatus() == Video.VideoStatus.APPROVED)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 基于内容的推荐
     */
    public List<Video> getContentBasedRecommendations(User user, int limit) {
        // 获取用户偏好的分类
        Pageable categoryPageable = PageRequest.of(0, 5);
        List<Object[]> preferredCategories = viewHistoryRepository.findUserPreferredCategories(user, categoryPageable);
        
        // 获取用户偏好的标签
        Pageable tagPageable = PageRequest.of(0, 10);
        List<Object[]> preferredTags = viewHistoryRepository.findUserPreferredTags(user, tagPageable);
        
        List<Video> recommendations = new ArrayList<>();
        
        // 基于分类推荐
        for (Object[] categoryResult : preferredCategories) {
            String categoryName = (String) categoryResult[0];
            List<Video> categoryVideos = videoService.getVideosByCategory(categoryName, limit / 2);
            recommendations.addAll(categoryVideos);
        }
        
        // 基于标签推荐
        for (Object[] tagResult : preferredTags) {
            String tagName = (String) tagResult[0];
            List<Video> tagVideos = videoService.getVideosByTag(tagName, limit / 2);
            recommendations.addAll(tagVideos);
        }
        
        // 过滤用户已观看的视频
        Set<Long> watchedVideoIds = getUserWatchedVideoIds(user);
        
        return recommendations.stream()
                .filter(video -> video.getStatus() == Video.VideoStatus.APPROVED)
                .filter(video -> !watchedVideoIds.contains(video.getId()))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门视频推荐
     */
    public List<Video> getPopularVideos(int limit) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7); // 最近7天
        Pageable pageable = PageRequest.of(0, limit);
        
        List<Object[]> results = viewHistoryRepository.findTrendingVideos(startDate, 5L, pageable);
        
        if (results.isEmpty()) {
            // 如果没有足够的观看数据，返回系统推荐的热门视频
            return videoService.getTopVideos(limit);
        }
        
        return results.stream()
                .map(result -> (Video) result[0])
                .filter(video -> video.getStatus() == Video.VideoStatus.APPROVED)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户观看历史
     */
    public List<ViewHistory> getUserViewHistory(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return viewHistoryRepository.findRecentViewHistory(user, pageable);
    }

    /**
     * 获取用户已观看的视频ID集合
     */
    private Set<Long> getUserWatchedVideoIds(User user) {
        List<ViewHistory> viewHistories = viewHistoryRepository.findByUserOrderByUpdatedAtDesc(user);
        return viewHistories.stream()
                .map(vh -> vh.getVideo().getId())
                .collect(Collectors.toSet());
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 检测设备类型
     */
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }

    /**
     * 清除用户观看历史
     */
    public void clearUserViewHistory(User user) {
        viewHistoryRepository.deleteByUser(user);
    }

    /**
     * 获取用户观看统计
     */
    public Map<String, Object> getUserViewingStats(User user) {
        Object[] stats = viewHistoryRepository.getUserViewingStats(user);
        Map<String, Object> result = new HashMap<>();
        
        if (stats != null && stats.length >= 3) {
            result.put("totalViews", stats[0] != null ? ((Number) stats[0]).longValue() : 0L);
            result.put("totalWatchTime", stats[1] != null ? ((Number) stats[1]).longValue() : 0L);
            result.put("averageCompletion", stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0);
        } else {
            result.put("totalViews", 0L);
            result.put("totalWatchTime", 0L);
            result.put("averageCompletion", 0.0);
        }
        
        return result;
    }

    /**
     * 将时长字符串转换为秒数
     * 格式：MM:SS 或 HH:MM:SS
     */
    private Long parseDurationToSeconds(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return 0L;
        }

        String[] parts = duration.split(":");
        if (parts.length == 2) {
            // MM:SS 格式
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return (long) (minutes * 60 + seconds);
        } else if (parts.length == 3) {
            // HH:MM:SS 格式
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            return (long) (hours * 3600 + minutes * 60 + seconds);
        }

        return 0L;
    }
}
