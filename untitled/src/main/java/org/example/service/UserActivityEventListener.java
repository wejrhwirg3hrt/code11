package org.example.service;

import org.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户活动事件监听器
 */
@Component
public class UserActivityEventListener {

    private static final Logger logger = LoggerFactory.getLogger(UserActivityEventListener.class);

    @Autowired
    private UserLevelService userLevelService;

    /**
     * 处理视频上传事件
     */
    @EventListener
    @Transactional
    public void handleVideoUpload(VideoUploadEvent event) {
        try {
            // 增加上传视频统计
            User user = event.getUser();
            userLevelService.addExperience(user, 50L, "上传视频");
            
            logger.info("用户 {} 上传视频，获得50经验值", user.getId());
        } catch (Exception e) {
            logger.error("处理视频上传事件失败: {}", e.getMessage());
        }
    }

    /**
     * 处理点赞事件
     */
    @EventListener
    @Transactional
    public void handleVideoLike(VideoLikeEvent event) {
        try {
            // 给被点赞的视频作者增加经验
            User videoOwner = event.getVideoOwner();
            userLevelService.addExperience(videoOwner, 5L, "视频被点赞");
            
            logger.info("用户 {} 的视频被点赞，获得5经验值", videoOwner.getId());
        } catch (Exception e) {
            logger.error("处理点赞事件失败: {}", e.getMessage());
        }
    }

    /**
     * 处理评论事件
     */
    @EventListener
    @Transactional
    public void handleComment(CommentEvent event) {
        try {
            // 给评论者增加经验
            User commenter = event.getCommenter();
            userLevelService.addExperience(commenter, 10L, "发表评论");
            
            logger.info("用户 {} 发表评论，获得10经验值", commenter.getId());
        } catch (Exception e) {
            logger.error("处理评论事件失败: {}", e.getMessage());
        }
    }

    /**
     * 处理观看事件
     */
    @EventListener
    @Transactional
    public void handleVideoWatch(VideoWatchEvent event) {
        try {
            // 根据观看时长给予经验（每10分钟1经验值）
            User viewer = event.getViewer();
            long watchDuration = event.getWatchDuration(); // 秒
            long expGained = watchDuration / 600; // 每10分钟1经验值
            
            if (expGained > 0) {
                userLevelService.addExperience(viewer, expGained, "观看视频");
                logger.info("用户 {} 观看视频{}秒，获得{}经验值", 
                        viewer.getId(), watchDuration, expGained);
            }
        } catch (Exception e) {
            logger.error("处理观看事件失败: {}", e.getMessage());
        }
    }

    /**
     * 处理每日登录事件
     */
    @EventListener
    @Transactional
    public void handleDailyLogin(DailyLoginEvent event) {
        try {
            User user = event.getUser();
            userLevelService.addExperience(user, 20L, "每日登录");
            
            logger.info("用户 {} 每日登录，获得20经验值", user.getId());
        } catch (Exception e) {
            logger.error("处理每日登录事件失败: {}", e.getMessage());
        }
    }

    // 事件类定义
    public static class VideoUploadEvent {
        private final User user;
        private final String videoTitle;

        public VideoUploadEvent(User user, String videoTitle) {
            this.user = user;
            this.videoTitle = videoTitle;
        }

        public User getUser() { return user; }
        public String getVideoTitle() { return videoTitle; }
    }

    public static class VideoLikeEvent {
        private final User videoOwner;
        private final User liker;
        private final Long videoId;

        public VideoLikeEvent(User videoOwner, User liker, Long videoId) {
            this.videoOwner = videoOwner;
            this.liker = liker;
            this.videoId = videoId;
        }

        public User getVideoOwner() { return videoOwner; }
        public User getLiker() { return liker; }
        public Long getVideoId() { return videoId; }
    }

    public static class CommentEvent {
        private final User commenter;
        private final Long videoId;
        private final String commentContent;

        public CommentEvent(User commenter, Long videoId, String commentContent) {
            this.commenter = commenter;
            this.videoId = videoId;
            this.commentContent = commentContent;
        }

        public User getCommenter() { return commenter; }
        public Long getVideoId() { return videoId; }
        public String getCommentContent() { return commentContent; }
    }

    public static class VideoWatchEvent {
        private final User viewer;
        private final Long videoId;
        private final long watchDuration; // 观看时长（秒）

        public VideoWatchEvent(User viewer, Long videoId, long watchDuration) {
            this.viewer = viewer;
            this.videoId = videoId;
            this.watchDuration = watchDuration;
        }

        public User getViewer() { return viewer; }
        public Long getVideoId() { return videoId; }
        public long getWatchDuration() { return watchDuration; }
    }

    public static class DailyLoginEvent {
        private final User user;
        private final boolean isConsecutive;

        public DailyLoginEvent(User user, boolean isConsecutive) {
            this.user = user;
            this.isConsecutive = isConsecutive;
        }

        public User getUser() { return user; }
        public boolean isConsecutive() { return isConsecutive; }
    }
}
