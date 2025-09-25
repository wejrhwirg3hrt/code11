package org.example.service;

import org.example.entity.VideoFavorite;
import org.example.entity.User;
import org.example.entity.Video;
import org.example.repository.VideoFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class VideoFavoriteService {

    @Autowired
    private VideoFavoriteRepository videoFavoriteRepository;

    /**
     * 收藏视频
     */
    @Transactional
    public boolean favoriteVideo(User user, Video video) {
        // 检查是否已经收藏
        if (videoFavoriteRepository.existsByUserAndVideo(user, video)) {
            return false; // 已经收藏过了
        }

        // 创建收藏记录
        VideoFavorite videoFavorite = new VideoFavorite(user, video);
        videoFavoriteRepository.save(videoFavorite);

        return true;
    }

    /**
     * 取消收藏
     */
    @Transactional
    public boolean unfavoriteVideo(User user, Video video) {
        Optional<VideoFavorite> videoFavorite = videoFavoriteRepository.findByUserAndVideo(user, video);
        if (videoFavorite.isPresent()) {
            videoFavoriteRepository.delete(videoFavorite.get());
            return true;
        }
        return false;
    }

    /**
     * 检查用户是否已收藏视频
     */
    public boolean isVideoFavoritedByUser(User user, Video video) {
        return videoFavoriteRepository.existsByUserAndVideo(user, video);
    }

    /**
     * 获取视频的收藏数
     */
    public long getVideoFavoriteCount(Video video) {
        return videoFavoriteRepository.countByVideo(video);
    }

    /**
     * 获取用户收藏的视频列表
     */
    public List<VideoFavorite> getUserFavoritedVideos(User user) {
        return videoFavoriteRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 删除视频的所有收藏记录
     */
    @Transactional
    public void deleteByVideoId(Long videoId) {
        videoFavoriteRepository.deleteByVideoId(videoId);
    }
} 