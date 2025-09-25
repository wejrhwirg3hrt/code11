package org.example.service;

import org.example.entity.VideoLike;
import org.example.entity.User;
import org.example.entity.Video;
import org.example.repository.VideoLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class VideoLikeService {

    @Autowired
    private VideoLikeRepository videoLikeRepository;
    
    @Autowired
    private AchievementService achievementService;

    /**
     * 点赞视频
     */
    @Transactional
    public boolean likeVideo(User user, Video video) {
        // 检查是否已经点赞
        if (videoLikeRepository.existsByUserAndVideo(user, video)) {
            return false; // 已经点赞过了
        }

        // 创建点赞记录
        VideoLike videoLike = new VideoLike(user, video);
        videoLikeRepository.save(videoLike);

        // 触发点赞相关成就检查
        achievementService.triggerAchievementCheck(user, "LIKE_VIDEO", 1);
        
        // 触发被点赞用户的成就检查
        if (video.getUser() != null) {
            achievementService.triggerAchievementCheck(video.getUser(), "RECEIVE_LIKE", 1);
        }

        return true;
    }

    /**
     * 取消点赞
     */
    @Transactional
    public boolean unlikeVideo(User user, Video video) {
        Optional<VideoLike> videoLike = videoLikeRepository.findByUserAndVideo(user, video);
        if (videoLike.isPresent()) {
            videoLikeRepository.delete(videoLike.get());
            return true;
        }
        return false;
    }

    /**
     * 检查用户是否已点赞视频
     */
    public boolean isVideoLikedByUser(User user, Video video) {
        return videoLikeRepository.existsByUserAndVideo(user, video);
    }

    /**
     * 获取视频的点赞数
     */
    public long getVideoLikeCount(Video video) {
        return videoLikeRepository.countByVideo(video);
    }

    /**
     * 获取用户点赞的视频列表
     */
    public List<VideoLike> getUserLikedVideos(User user) {
        return videoLikeRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 获取用户给出的总点赞数
     */
    public long getUserTotalLikesGiven(Long userId) {
        return videoLikeRepository.countByUserId(userId);
    }

    /**
     * 获取用户收到的总点赞数
     */
    public long getUserTotalLikesReceived(Long userId) {
        return videoLikeRepository.countByVideoUserId(userId);
    }

    /**
     * 删除视频的所有点赞记录
     */
    @Transactional
    public void deleteByVideoId(Long videoId) {
        videoLikeRepository.deleteByVideoId(videoId);
    }
}
