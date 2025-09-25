package org.example.service;

import org.example.entity.User;
import org.example.entity.UserFollow;
import org.example.repository.UserFollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 关注服务
 */
@Service
@Transactional(readOnly = false)
public class FollowService {

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AchievementService achievementService;

    /**
     * 关注用户
     */
    public boolean followUser(User follower, User following) {
        // 不能关注自己
        if (follower.getId().equals(following.getId())) {
            return false;
        }

        // 检查是否已经关注
        if (isFollowing(follower.getId(), following.getId())) {
            return false;
        }

        // 创建关注关系
        UserFollow userFollow = new UserFollow();
        userFollow.setFollower(follower);
        userFollow.setFollowing(following);
        userFollow.setCreatedAt(LocalDateTime.now());

        userFollowRepository.save(userFollow);

        // 发送关注通知
        notificationService.notifyUserFollow(following, follower);

        // 触发关注成就检查
        try {
            achievementService.triggerAchievementCheck(follower, "FOLLOW_USER", 1);
            achievementService.triggerAchievementCheck(following, "RECEIVE_FOLLOW", 1);
        } catch (Exception e) {
            System.err.println("❌ 关注成就检查失败: " + e.getMessage());
        }

        return true;
    }

    /**
     * 取消关注用户
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollowUser(User follower, User following) {
        try {
            System.out.println("🔍 开始取消关注操作:");
            System.out.println("  关注者ID: " + follower.getId() + ", 用户名: " + follower.getUsername());
            System.out.println("  被关注者ID: " + following.getId() + ", 用户名: " + following.getUsername());
            
            // 检查关注关系是否存在
            boolean exists = userFollowRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId());
            System.out.println("  关注关系是否存在: " + exists);
            
            if (!exists) {
                System.out.println("⚠️ 关注关系不存在，无需删除");
                return true; // 关系不存在，认为操作成功
            }
            
            // 直接删除关注关系
            int deletedCount = userFollowRepository.deleteByFollowerIdAndFollowingId(follower.getId(), following.getId());
            System.out.println("✅ 删除关注关系，影响记录数: " + deletedCount);
                
                // 验证删除结果
                boolean stillExists = userFollowRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId());
                System.out.println("  删除后关注关系是否仍存在: " + stillExists);
                
            boolean success = !stillExists;
            System.out.println("  取消关注操作结果: " + (success ? "成功" : "失败"));
            
            return success;
            
        } catch (Exception e) {
            System.err.println("❌ 取消关注失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查是否关注
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    /**
     * 获取用户关注列表
     */
    public Page<UserFollow> getFollowingList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userFollowRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取用户粉丝列表
     */
    public Page<UserFollow> getFollowersList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userFollowRepository.findByFollowingIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取关注数量
     */
    public long getFollowingCount(Long userId) {
        return userFollowRepository.countByFollowerId(userId);
    }

    /**
     * 获取粉丝数量
     */
    public long getFollowersCount(Long userId) {
        return userFollowRepository.countByFollowingId(userId);
    }

    /**
     * 获取互相关注的用户列表
     */
    public List<UserFollow> getMutualFollows(Long userId) {
        return userFollowRepository.findMutualFollows(userId);
    }

    /**
     * 检查是否互相关注
     */
    public boolean isMutualFollow(Long userId1, Long userId2) {
        return isFollowing(userId1, userId2) && isFollowing(userId2, userId1);
    }

    /**
     * 获取用户的关注建议
     * 基于共同关注的用户推荐
     */
    public List<User> getFollowSuggestions(Long userId, int limit) {
        return userFollowRepository.findFollowSuggestions(userId, PageRequest.of(0, limit));
    }

    /**
     * 获取最近关注的用户
     */
    public List<UserFollow> getRecentFollows(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return userFollowRepository.findTop10ByFollowerIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取最近的粉丝
     */
    public List<UserFollow> getRecentFollowers(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return userFollowRepository.findTop10ByFollowingIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取关注我的用户列表
     */
    public List<User> getFollowers(Long userId) {
        List<UserFollow> follows = userFollowRepository.findByFollowingId(userId);
        return follows.stream()
                .map(UserFollow::getFollower)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取我关注的用户列表
     */
    public List<UserFollow> getFollowing(Long userId) {
        return userFollowRepository.findByFollowerIdAndAcceptedTrue(userId);
    }

    /**
     * 批量关注用户
     */
    public int batchFollowUsers(User follower, List<Long> followingIds) {
        int successCount = 0;
        for (Long followingId : followingIds) {
            if (!followingId.equals(follower.getId()) && !isFollowing(follower.getId(), followingId)) {
                UserFollow userFollow = new UserFollow();
                userFollow.setFollower(follower);
                // 这里需要根据ID获取User对象，简化处理
                User following = new User();
                following.setId(followingId);
                userFollow.setFollowing(following);
                userFollow.setCreatedAt(LocalDateTime.now());
                
                userFollowRepository.save(userFollow);
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 获取用户社交统计信息
     */
    public UserSocialStats getUserSocialStats(Long userId) {
        long followingCount = getFollowingCount(userId);
        long followersCount = getFollowersCount(userId);
        long mutualCount = getMutualFollows(userId).size();
        
        return new UserSocialStats(followingCount, followersCount, mutualCount);
    }

    /**
     * 用户社交统计信息类
     */
    public static class UserSocialStats {
        private long followingCount;
        private long followersCount;
        private long mutualCount;

        public UserSocialStats(long followingCount, long followersCount, long mutualCount) {
            this.followingCount = followingCount;
            this.followersCount = followersCount;
            this.mutualCount = mutualCount;
        }

        // Getters
        public long getFollowingCount() { return followingCount; }
        public long getFollowersCount() { return followersCount; }
        public long getMutualCount() { return mutualCount; }
    }

    /**
     * 清理无效的关注关系
     * 删除用户不存在的关注关系
     */
    @Transactional
    public void cleanupInvalidFollows() {
        // 这里可以实现清理逻辑
        // 删除follower或following用户不存在的记录
    }

    /**
     * 清理重复的关注记录
     * 删除同一用户对另一用户的重复关注记录，只保留一条
     */
    @Transactional
    public void cleanupDuplicateFollows() {
        try {
            System.out.println("🔍 开始清理重复的关注记录...");
            
            // 查找所有重复的关注记录
            List<Object[]> duplicates = userFollowRepository.findDuplicateFollows();
            
            if (duplicates.isEmpty()) {
                System.out.println("✅ 没有发现重复的关注记录");
                return;
            }
            
            System.out.println("发现 " + duplicates.size() + " 组重复的关注记录");
            
            int deletedCount = 0;
            for (Object[] duplicate : duplicates) {
                Long followerId = (Long) duplicate[0];
                Long followingId = (Long) duplicate[1];
                Long count = (Long) duplicate[2];
                
                if (count > 1) {
                    // 保留第一条记录，删除其余重复记录
                    List<UserFollow> records = userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId);
                    if (records.size() > 1) {
                        // 删除除第一条外的所有记录
                        List<UserFollow> toDelete = records.subList(1, records.size());
                        userFollowRepository.deleteAll(toDelete);
                        deletedCount += toDelete.size();
                        System.out.println("删除用户 " + followerId + " 对用户 " + followingId + " 的 " + toDelete.size() + " 条重复关注记录");
                    }
                }
            }
            
            System.out.println("✅ 清理完成，共删除 " + deletedCount + " 条重复的关注记录");
            
        } catch (Exception e) {
            System.err.println("❌ 清理重复关注记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取活跃关注者
     * 最近有互动的关注者
     */
    public List<UserFollow> getActiveFollowers(Long userId, int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(0, limit);
        return userFollowRepository.findActiveFollowers(userId, since, pageable);
    }
}
