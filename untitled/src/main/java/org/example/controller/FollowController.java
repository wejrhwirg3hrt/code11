package org.example.controller;

import org.example.entity.User;
import org.example.service.FollowService;
import org.example.service.UserService;
import org.example.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import org.example.repository.UserFollowRepository;

/**
 * 关注功能控制器
 */
@RestController
@RequestMapping("/api")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserFollowRepository userFollowRepository;

    /**
     * 关注/取消关注用户
     */
    @PostMapping("/follow/toggle")
    public ResponseEntity<Map<String, Object>> toggleFollow(@RequestBody Map<String, Object> request,
                                                           Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }

            // 从请求体中获取userId
            Long userId = Long.valueOf(request.get("userId").toString());

            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "当前用户不存在");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> targetUserOpt = userService.findById(userId);
            if (!targetUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "目标用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User targetUser = targetUserOpt.get();

            User currentUser = currentUserOpt.get();

            // 不能关注自己
            if (currentUser.getId().equals(targetUser.getId())) {
                response.put("success", false);
                response.put("message", "不能关注自己");
                return ResponseEntity.ok(response);
            }

            boolean isFollowing = followService.isFollowing(currentUser.getId(), targetUser.getId());

            if (isFollowing) {
                // 取消关注
                boolean success = followService.unfollowUser(currentUser, targetUser);
                response.put("success", success);
                response.put("isFollowing", false);
                response.put("message", success ? "取消关注成功" : "取消关注失败");

                System.out.println(String.format("用户 %s 取消关注 %s，结果: %s",
                    currentUser.getUsername(), targetUser.getUsername(), success));
            } else {
                // 关注
                boolean success = followService.followUser(currentUser, targetUser);
                response.put("success", success);
                response.put("isFollowing", true);
                response.put("message", success ? "关注成功" : "关注失败");

                System.out.println(String.format("用户 %s 关注 %s，结果: %s",
                    currentUser.getUsername(), targetUser.getUsername(), success));

                // 触发关注成就检查
                if (success) {
                    try {
                        // 触发关注者的关注成就
                        achievementService.triggerAchievementCheck(currentUser, "FOLLOW_USER");
                        // 触发被关注者的获得关注成就
                        achievementService.triggerAchievementCheck(targetUser, "RECEIVE_FOLLOW");
                        System.out.println("✅ 关注成就检查完成");
                    } catch (Exception achievementError) {
                        System.err.println("❌ 关注成就检查失败: " + achievementError.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("关注操作异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 关注/取消关注用户 (路径参数版本)
     */
    @PostMapping("/follow/user/{userId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleFollowByPath(@PathVariable Long userId,
                                                                 Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "当前用户不存在");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> targetUserOpt = userService.findById(userId);
            if (!targetUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "目标用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User targetUser = targetUserOpt.get();
            User currentUser = currentUserOpt.get();

            // 不能关注自己
            if (currentUser.getId().equals(targetUser.getId())) {
                response.put("success", false);
                response.put("message", "不能关注自己");
                return ResponseEntity.ok(response);
            }

            boolean isFollowing = followService.isFollowing(currentUser.getId(), targetUser.getId());

            if (isFollowing) {
                // 取消关注
                boolean success = followService.unfollowUser(currentUser, targetUser);
                response.put("success", success);
                response.put("isFollowing", false);
                response.put("message", success ? "取消关注成功" : "取消关注失败");
            } else {
                // 关注
                boolean success = followService.followUser(currentUser, targetUser);
                response.put("success", success);
                response.put("isFollowing", true);
                response.put("message", success ? "关注成功" : "关注失败");

                // 触发关注成就检查
                if (success) {
                    try {
                        // 触发关注者的关注成就
                        achievementService.triggerAchievementCheck(currentUser, "FOLLOW_USER");
                        // 触发被关注者的获得关注成就
                        achievementService.triggerAchievementCheck(targetUser, "RECEIVE_FOLLOW");
                        System.out.println("✅ 关注成就检查完成");
                    } catch (Exception achievementError) {
                        System.err.println("❌ 关注成就检查失败: " + achievementError.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("关注操作失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 检查是否关注某用户
     */
    @GetMapping("/user/{userId}/follow/status")
    public ResponseEntity<Map<String, Object>> getFollowStatus(@PathVariable Long userId,
                                                              Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("following", false);
            return ResponseEntity.ok(response);
        }

        Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
        if (!currentUserOpt.isPresent()) {
            response.put("following", false);
            return ResponseEntity.ok(response);
        }

        boolean isFollowing = followService.isFollowing(currentUserOpt.get().getId(), userId);
        response.put("following", isFollowing);

        return ResponseEntity.ok(response);
    }

    /**
     * 检查关注状态（新的API路径）
     */
    @GetMapping("/follow/status/{userId}")
    public ResponseEntity<Map<String, Object>> getFollowStatusNew(@PathVariable Long userId,
                                                                 Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("isFollowing", false);
            response.put("isMutual", false);
            return ResponseEntity.ok(response);
        }

        try {
            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("isFollowing", false);
                response.put("isMutual", false);
                return ResponseEntity.ok(response);
            }

            User currentUser = currentUserOpt.get();
            boolean isFollowing = followService.isFollowing(currentUser.getId(), userId);
            boolean isMutual = followService.isMutualFollow(currentUser.getId(), userId);

            response.put("success", true);
            response.put("isFollowing", isFollowing);
            response.put("isMutual", isMutual);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取关注状态失败: " + e.getMessage());
            response.put("isFollowing", false);
            response.put("isMutual", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 关注用户
     */
    @PostMapping("/follow/{userId}")
    public ResponseEntity<Map<String, Object>> followUser(@PathVariable Long userId,
                                                         Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            Optional<User> targetUserOpt = userService.findById(userId);
            if (!targetUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "目标用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User currentUser = currentUserOpt.get();
            User targetUser = targetUserOpt.get();

            boolean success = followService.followUser(currentUser, targetUser);
            if (success) {
                response.put("success", true);
                response.put("message", "关注成功");
                response.put("isFollowing", true);
                response.put("isMutual", followService.isMutualFollow(currentUser.getId(), userId));
            } else {
                response.put("success", false);
                response.put("message", "关注失败，可能已经关注过了");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "关注失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 取消关注用户
     */
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<Map<String, Object>> unfollowUser(@PathVariable Long userId,
                                                           Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            Optional<User> targetUserOpt = userService.findById(userId);
            if (!targetUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "目标用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User currentUser = currentUserOpt.get();
            User targetUser = targetUserOpt.get();

            boolean success = followService.unfollowUser(currentUser, targetUser);
            if (success) {
                response.put("success", true);
                response.put("message", "取消关注成功");
                response.put("isFollowing", false);
                response.put("isMutual", false);
            } else {
                response.put("success", false);
                response.put("message", "取消关注失败");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消关注失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取当前用户的粉丝数量
     */
    @GetMapping("/follow/followers/count")
    public ResponseEntity<Map<String, Object>> getFollowersCount(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("success", false);
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        try {
            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("success", false);
                response.put("count", 0);
                return ResponseEntity.ok(response);
            }

            User currentUser = currentUserOpt.get();
            long followersCount = followService.getFollowersCount(currentUser.getId());

            response.put("success", true);
            response.put("count", followersCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("count", 0);
            response.put("message", "获取粉丝数量失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取关注我的用户列表
     */
    @GetMapping("/follow/followers")
    public ResponseEntity<Map<String, Object>> getFollowers(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            response.put("data", new java.util.ArrayList<>());
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                response.put("data", new java.util.ArrayList<>());
                return ResponseEntity.status(404).body(response);
            }

            User currentUser = currentUserOpt.get();
            java.util.List<User> followers = followService.getFollowers(currentUser.getId());

            // 转换为前端需要的格式
            java.util.List<Map<String, Object>> followersList = new java.util.ArrayList<>();
            for (User follower : followers) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", follower.getId());
                userInfo.put("username", follower.getUsername());
                userInfo.put("nickname", follower.getNickname());
                userInfo.put("avatar", follower.getAvatar());
                userInfo.put("bio", follower.getBio());

                // 检查是否互相关注
                boolean isMutual = followService.isFollowing(currentUser.getId(), follower.getId());
                userInfo.put("isFollowing", isMutual);
                userInfo.put("isMutual", isMutual);

                followersList.add(userInfo);
            }

            response.put("success", true);
            response.put("data", followersList);
            response.put("count", followersList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取关注者列表失败: " + e.getMessage());
            response.put("data", new java.util.ArrayList<>());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取用户关注列表
     */
    @GetMapping("/user/{userId}/following")
    public ResponseEntity<Map<String, Object>> getFollowingList(@PathVariable Long userId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var followingList = followService.getFollowingList(userId, page, size);
            response.put("success", true);
            response.put("data", followingList);
            response.put("totalElements", followingList.getTotalElements());
            response.put("totalPages", followingList.getTotalPages());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取关注列表失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户粉丝列表
     */
    @GetMapping("/user/{userId}/followers")
    public ResponseEntity<Map<String, Object>> getFollowersList(@PathVariable Long userId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var followersList = followService.getFollowersList(userId, page, size);
            response.put("success", true);
            response.put("data", followersList);
            response.put("totalElements", followersList.getTotalElements());
            response.put("totalPages", followersList.getTotalPages());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取粉丝列表失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户社交统计
     */
    @GetMapping("/user/{userId}/social-stats")
    public ResponseEntity<Map<String, Object>> getSocialStats(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var stats = followService.getUserSocialStats(userId);
            response.put("success", true);
            response.put("followingCount", stats.getFollowingCount());
            response.put("followersCount", stats.getFollowersCount());
            response.put("mutualCount", stats.getMutualCount());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取关注建议
     */
    @GetMapping("/user/follow-suggestions")
    public ResponseEntity<Map<String, Object>> getFollowSuggestions(Authentication authentication,
                                                                   @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.ok(response);
        }

        Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
        if (!currentUserOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }

        try {
            var suggestions = followService.getFollowSuggestions(currentUserOpt.get().getId(), limit);
            response.put("success", true);
            response.put("data", suggestions);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取推荐失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取互相关注的用户
     */
    @GetMapping("/user/{userId}/mutual-follows")
    public ResponseEntity<Map<String, Object>> getMutualFollows(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var mutualFollows = followService.getMutualFollows(userId);
            response.put("success", true);
            response.put("data", mutualFollows);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取互关用户失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 清理重复的关注记录（管理员功能）
     */
    @PostMapping("/follow/cleanup-duplicates")
    public ResponseEntity<Map<String, Object>> cleanupDuplicateFollows(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User currentUser = currentUserOpt.get();
            
            // 检查是否为管理员
            if (!"admin".equals(currentUser.getUsername()) && !"superadmin".equals(currentUser.getUsername())) {
                response.put("success", false);
                response.put("message", "权限不足，只有管理员可以执行此操作");
                return ResponseEntity.status(403).body(response);
            }

            // 执行清理操作
            followService.cleanupDuplicateFollows();
            
            response.put("success", true);
            response.put("message", "重复关注记录清理完成");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("清理重复关注记录失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "清理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 检查重复的关注记录（管理员功能）
     */
    @GetMapping("/follow/check-duplicates")
    public ResponseEntity<Map<String, Object>> checkDuplicateFollows(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
            if (!currentUserOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User currentUser = currentUserOpt.get();
            
            // 检查是否为管理员
            if (!"admin".equals(currentUser.getUsername()) && !"superadmin".equals(currentUser.getUsername())) {
                response.put("success", false);
                response.put("message", "权限不足，只有管理员可以执行此操作");
                return ResponseEntity.status(403).body(response);
            }

            // 查找重复记录
            List<Object[]> duplicates = userFollowRepository.findDuplicateFollows();
            
            List<Map<String, Object>> duplicateList = new ArrayList<>();
            for (Object[] duplicate : duplicates) {
                Map<String, Object> dup = new HashMap<>();
                dup.put("followerId", duplicate[0]);
                dup.put("followingId", duplicate[1]);
                dup.put("count", duplicate[2]);
                duplicateList.add(dup);
            }
            
            response.put("success", true);
            response.put("message", "检查完成，发现 " + duplicates.size() + " 组重复记录");
            response.put("duplicates", duplicateList);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("检查重复关注记录失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "检查失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 关注记录清理页面
     */
    @GetMapping("/follow/cleanup")
    public String showCleanupPage() {
        return "follow-cleanup";
    }


}
