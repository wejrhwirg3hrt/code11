package org.example.controller.api;

import org.example.entity.*;
import org.example.repository.*;
import org.example.service.UserService;
import org.example.service.NotificationService;
import org.example.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
public class VideoInteractionController {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private VideoFavoriteRepository videoFavoriteRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AchievementService achievementService;

    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<Map<String, Object>> addLike(@PathVariable Long id, Authentication authentication) {
        return handleLikeToggle(id, authentication, false);
    }

    @DeleteMapping("/{id}/like")
    @Transactional
    public ResponseEntity<Map<String, Object>> removeLike(@PathVariable Long id, Authentication authentication) {
        return handleLikeToggle(id, authentication, true);
    }

    private ResponseEntity<Map<String, Object>> handleLikeToggle(@PathVariable Long id, Authentication authentication, boolean forceRemove) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        Optional<Video> videoOpt = videoRepository.findById(id);

        if (!userOpt.isPresent() || !videoOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Video video = videoOpt.get();

        Optional<VideoLike> existingLike = videoLikeRepository.findByUserAndVideo(user, video);

        if (forceRemove || existingLike.isPresent()) {
            // 取消点赞
            if (existingLike.isPresent()) {
                videoLikeRepository.delete(existingLike.get());
                video.setLikeCount(Math.max(0, video.getLikeCount() - 1));
            }
        } else {
            // 添加点赞
            VideoLike like = new VideoLike();
            like.setUser(user);
            like.setVideo(video);
            videoLikeRepository.save(like);
            video.setLikeCount(video.getLikeCount() + 1);

            // 发送点赞通知
            if (video.getUser() != null) {
                notificationService.createLikeNotification(user, video.getUser(), video);

                // 触发成就检查
                try {
                    // 触发点赞者的点赞成就检查
                    achievementService.triggerAchievementCheck(user, "LIKE_VIDEO", 1);

                    // 触发被点赞者的获得点赞成就检查
                    achievementService.triggerAchievementCheck(video.getUser(), "RECEIVE_LIKE", 1);

                    System.out.println("✅ 点赞成就检查完成");
                } catch (Exception e) {
                    System.err.println("❌ 点赞成就检查失败: " + e.getMessage());
                }
            }
        }

        videoRepository.save(video);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("likeCount", video.getLikeCount());
        response.put("liked", !existingLike.isPresent());
        response.put("message", !existingLike.isPresent() ? "点赞成功" : "取消点赞");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/favorite")
    @Transactional
    public ResponseEntity<Map<String, Object>> addFavorite(@PathVariable Long id, Authentication authentication) {
        return handleFavoriteToggle(id, authentication, false);
    }

    @DeleteMapping("/{id}/favorite")
    @Transactional
    public ResponseEntity<Map<String, Object>> removeFavorite(@PathVariable Long id, Authentication authentication) {
        return handleFavoriteToggle(id, authentication, true);
    }

    private ResponseEntity<Map<String, Object>> handleFavoriteToggle(@PathVariable Long id, Authentication authentication, boolean forceRemove) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        Optional<Video> videoOpt = videoRepository.findById(id);

        if (!userOpt.isPresent() || !videoOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Video video = videoOpt.get();

        Optional<VideoFavorite> existingFavorite = videoFavoriteRepository.findByUserAndVideo(user, video);

        if (forceRemove || existingFavorite.isPresent()) {
            // 取消收藏
            if (existingFavorite.isPresent()) {
                videoFavoriteRepository.delete(existingFavorite.get());
                video.setFavoriteCount(Math.max(0, video.getFavoriteCount() - 1));
            }
        } else {
            // 添加收藏
            VideoFavorite favorite = new VideoFavorite();
            favorite.setUser(user);
            favorite.setVideo(video);
            videoFavoriteRepository.save(favorite);
            video.setFavoriteCount(video.getFavoriteCount() + 1);

            // 发送收藏通知
            if (video.getUser() != null) {
                notificationService.createFavoriteNotification(user, video.getUser(), video);
            }

            // 触发成就检查
            try {
                // 触发收藏者的收藏成就检查
                achievementService.triggerAchievementCheck(user, "FAVORITE_VIDEO");

                // 触发被收藏者的获得收藏成就检查
                if (video.getUser() != null) {
                    achievementService.triggerAchievementCheck(video.getUser(), "RECEIVE_FAVORITE");
                }

                System.out.println("✅ 收藏成就检查完成");
            } catch (Exception e) {
                System.err.println("❌ 收藏成就检查失败: " + e.getMessage());
            }
        }

        videoRepository.save(video);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("favoriteCount", video.getFavoriteCount());
        response.put("favorited", !existingFavorite.isPresent());
        response.put("message", !existingFavorite.isPresent() ? "收藏成功" : "取消收藏");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/view")
    @Transactional
    public ResponseEntity<Map<String, Object>> incrementView(@PathVariable Long id, Authentication authentication) {
        Optional<Video> videoOpt = videoRepository.findById(id);

        if (!videoOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Video video = videoOpt.get();

        // 检查是否是视频所有者
        boolean isOwner = false;
        if (authentication != null) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent() && video.getUser() != null &&
                    video.getUser().getId().equals(userOpt.get().getId())) {
                isOwner = true;
            }
        }

        // 只有非所有者访问时才增加观看量
        if (!isOwner) {
            video.setViews(video.getViews() + 1);
            if (video.getViewCount() != null) {
                video.setViewCount(video.getViewCount() + 1);
            } else {
                video.setViewCount(1L);
            }
            videoRepository.save(video);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("views", video.getViews());
        response.put("viewCount", video.getViewCount());

        return ResponseEntity.ok(response);
    }
}