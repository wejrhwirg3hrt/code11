package org.example.controller;

import org.example.entity.User;
import org.example.entity.Video;
import org.example.service.AchievementService;
import org.example.service.FollowService;
import org.example.service.UserService;
import org.example.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 用户个人主页控制器
 */
@Controller
@RequestMapping("/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private FollowService followService;

    @Autowired
    private AchievementService achievementService;

    /**
     * 显示用户成就页面 - 必须在 /{userId} 路由之前
     */
    @GetMapping("/achievements")
    public String showAchievements(Model model, Authentication authentication) {
        // 简化版本 - 先测试基本功能
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";
        }

        try {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                return "redirect:/login";
            }

            User user = userOpt.get();

            // 添加基本用户信息
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            // 暂时添加空的成就信息，避免模板错误
            model.addAttribute("progressInfo", null);

            return "user-achievements";

        } catch (Exception e) {
            System.err.println("显示成就页面失败: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }


    /**
     * 查看用户个人主页 - 支持 /user/profile/{userId} 路径
     */
    @GetMapping("/profile/{userId:[0-9]+}")
    public String viewUserProfileWithPath(@PathVariable Long userId, Model model, Authentication authentication) {
        return viewUserProfile(userId, model, authentication);
    }

    /**
     * 查看用户个人主页 - 只匹配数字ID
     */
    @GetMapping("/{userId:[0-9]+}")
    public String viewUserProfile(@PathVariable Long userId, Model model, Authentication authentication) {
        // 获取目标用户信息
        Optional<User> targetUserOpt = userService.findById(userId);
        if (!targetUserOpt.isPresent()) {
            return "redirect:/";
        }
        
        User targetUser = targetUserOpt.get();
        
        // 获取当前登录用户
        User currentUser = null;
        if (authentication != null) {
            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            currentUser = currentUserOpt.orElse(null);
        }
        
        // 检查是否是自己的主页
        boolean isOwnProfile = currentUser != null && currentUser.getId().equals(userId);

        // 获取用户视频：如果是自己的主页显示所有视频，否则只显示已审核的视频
        List<Video> userVideos;
        if (isOwnProfile) {
            userVideos = videoService.getVideosByUser(targetUser);
        } else {
            userVideos = videoService.findApprovedVideosByUser(targetUser);
        }

        // 获取用户喜欢的视频（暂时为空列表，后续实现）
        List<Video> likedVideos = new ArrayList<>();

        // 获取用户收藏的视频（暂时为空列表，后续实现）
        List<Video> favoriteVideos = new ArrayList<>();

        // 计算用户统计信息
        UserStats userStats = calculateUserStats(targetUser, userVideos);

        // 检查关注状态
        boolean isFollowing = false;
        boolean isMutualFollow = false;
        if (currentUser != null && !isOwnProfile) {
            isFollowing = followService.isFollowing(currentUser.getId(), targetUser.getId());
            if (isFollowing) {
                isMutualFollow = followService.isMutualFollow(currentUser.getId(), targetUser.getId());
            }
        }

        model.addAttribute("targetUser", targetUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("isMutualFollow", isMutualFollow);
        model.addAttribute("userVideos", userVideos);
        model.addAttribute("likedVideos", likedVideos);
        model.addAttribute("favoriteVideos", favoriteVideos);
        model.addAttribute("userStats", userStats);
        
        return "user-profile";
    }

    /**
     * 计算用户统计信息
     */
    private UserStats calculateUserStats(User user, List<Video> videos) {
        UserStats stats = new UserStats();
        stats.setTotalVideos(videos.size());
        
        long totalViews = videos.stream()
                .mapToLong(video -> video.getViews() != null ? video.getViews() : 0)
                .sum();
        stats.setTotalViews(totalViews);
        
        long totalLikes = videos.stream()
                .mapToLong(video -> video.getLikeCount() != null ? video.getLikeCount() : 0)
                .sum();
        stats.setTotalLikes(totalLikes);
        
        return stats;
    }

    /**
     * 用户统计信息内部类
     */
    public static class UserStats {
        private int totalVideos;
        private long totalViews;
        private long totalLikes;

        // Getters and Setters
        public int getTotalVideos() {
            return totalVideos;
        }

        public void setTotalVideos(int totalVideos) {
            this.totalVideos = totalVideos;
        }

        public long getTotalViews() {
            return totalViews;
        }

        public void setTotalViews(long totalViews) {
            this.totalViews = totalViews;
        }

        public long getTotalLikes() {
            return totalLikes;
        }

        public void setTotalLikes(long totalLikes) {
            this.totalLikes = totalLikes;
        }
    }

}
