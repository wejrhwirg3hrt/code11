package org.example.service;

import org.example.entity.UserFollow;
import org.example.repository.UserFollowRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 数据库修复服务
 * 用于修复数据一致性问题
 */
@Service
public class DatabaseRepairService {

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 修复关注关系数据
     */
    @Transactional
    public Map<String, Object> repairFollows() {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("🔧 开始修复关注关系数据...");
        
        try {
            // 1. 删除无效的关注关系（用户不存在的记录）
            int invalidCount = repairInvalidFollows();
            
            // 2. 清理重复的关注记录
            int duplicateCount = cleanupDuplicateFollows();
            
            // 3. 修复自关注记录
            int selfFollowCount = repairSelfFollows();
            
            System.out.println("✅ 关注关系数据修复完成");
            
            result.put("deletedDuplicates", duplicateCount);
            result.put("deletedSelfFollows", selfFollowCount);
            result.put("deletedInvalid", invalidCount);
            result.put("keptRecords", (int) userFollowRepository.count());
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ 修复关注关系数据失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取数据库统计信息
     */
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long totalFollows = userFollowRepository.count();
        
        // 检查重复记录
        List<Object[]> duplicates = userFollowRepository.findDuplicateFollows();
        int duplicateCount = 0;
        for (Object[] duplicate : duplicates) {
            Long count = (Long) duplicate[2];
            if (count > 1) {
                duplicateCount += count.intValue() - 1; // 只计算重复的部分
            }
        }
        
        // 检查自关注记录
        List<UserFollow> allFollows = userFollowRepository.findAll();
        long selfFollowCount = allFollows.stream()
            .filter(follow -> follow.getFollower().getId().equals(follow.getFollowing().getId()))
            .count();
        
        // 计算唯一关注关系数
        long uniqueFollows = totalFollows - duplicateCount - selfFollowCount;
        
        stats.put("totalUsers", totalUsers);
        stats.put("totalFollows", totalFollows);
        stats.put("duplicateCount", duplicateCount);
        stats.put("selfFollows", selfFollowCount);
        stats.put("uniqueFollows", uniqueFollows);
        
        return stats;
    }

    /**
     * 查找重复的关注记录
     */
    public List<Map<String, Object>> findDuplicateFollows() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        List<Object[]> duplicates = userFollowRepository.findDuplicateFollows();
        
        for (Object[] duplicate : duplicates) {
            Long followerId = (Long) duplicate[0];
            Long followingId = (Long) duplicate[1];
            Long count = (Long) duplicate[2];
            
            if (count > 1) {
                Map<String, Object> dupInfo = new HashMap<>();
                dupInfo.put("followerId", followerId);
                dupInfo.put("followingId", followingId);
                dupInfo.put("count", count);
                
                // 获取具体的记录信息
                List<UserFollow> records = userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId);
                List<Long> ids = new ArrayList<>();
                List<String> createdAts = new ArrayList<>();
                
                for (UserFollow record : records) {
                    ids.add(record.getId());
                    createdAts.add(record.getCreatedAt().toString());
                }
                
                dupInfo.put("ids", ids);
                dupInfo.put("createdAts", createdAts);
                
                result.add(dupInfo);
            }
        }
        
        return result;
    }

    /**
     * 删除无效的关注关系
     */
    private int repairInvalidFollows() {
        System.out.println("  1. 检查无效的关注关系...");
        
        // 查找所有关注记录
        List<UserFollow> allFollows = userFollowRepository.findAll();
        int invalidCount = 0;
        
        for (UserFollow follow : allFollows) {
            boolean followerExists = userRepository.existsById(follow.getFollower().getId());
            boolean followingExists = userRepository.existsById(follow.getFollowing().getId());
            
            if (!followerExists || !followingExists) {
                System.out.println("    删除无效关注关系: " + follow.getId() + 
                    " (关注者存在: " + followerExists + ", 被关注者存在: " + followingExists + ")");
                userFollowRepository.delete(follow);
                invalidCount++;
            }
        }
        
        System.out.println("    删除了 " + invalidCount + " 条无效的关注关系");
        return invalidCount;
    }

    /**
     * 清理重复的关注记录
     */
    private int cleanupDuplicateFollows() {
        System.out.println("  2. 清理重复的关注记录...");
        
        // 查找重复记录
        List<Object[]> duplicates = userFollowRepository.findDuplicateFollows();
        int deletedCount = 0;
        
        for (Object[] duplicate : duplicates) {
            Long followerId = (Long) duplicate[0];
            Long followingId = (Long) duplicate[1];
            Long count = (Long) duplicate[2];
            
            if (count > 1) {
                System.out.println("    发现重复关注: 用户 " + followerId + " 关注用户 " + followingId + " 共 " + count + " 次");
                
                // 保留第一条记录，删除其余重复记录
                List<UserFollow> records = userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId);
                if (records.size() > 1) {
                    // 删除除第一条外的所有记录
                    List<UserFollow> toDelete = records.subList(1, records.size());
                    userFollowRepository.deleteAll(toDelete);
                    deletedCount += toDelete.size();
                    System.out.println("      删除了 " + toDelete.size() + " 条重复记录");
                }
            }
        }
        
        System.out.println("    总共删除了 " + deletedCount + " 条重复的关注记录");
        return deletedCount;
    }

    /**
     * 修复自关注记录
     */
    private int repairSelfFollows() {
        System.out.println("  3. 修复自关注记录...");
        
        // 查找自关注记录
        List<UserFollow> allFollows = userFollowRepository.findAll();
        int selfFollowCount = 0;
        
        for (UserFollow follow : allFollows) {
            if (follow.getFollower().getId().equals(follow.getFollowing().getId())) {
                System.out.println("    删除自关注记录: 用户 " + follow.getFollower().getId());
                userFollowRepository.delete(follow);
                selfFollowCount++;
            }
        }
        
        System.out.println("    删除了 " + selfFollowCount + " 条自关注记录");
        return selfFollowCount;
    }

    /**
     * 获取数据库统计信息（控制台输出版本）
     */
    public void printDatabaseStats() {
        System.out.println("📊 数据库统计信息:");
        
        Map<String, Object> stats = getDatabaseStats();
        
        System.out.println("  总用户数: " + stats.get("totalUsers"));
        System.out.println("  总关注关系数: " + stats.get("totalFollows"));
        System.out.println("  重复关注记录数: " + stats.get("duplicateCount"));
        System.out.println("  自关注记录数: " + stats.get("selfFollows"));
        System.out.println("  唯一关注关系数: " + stats.get("uniqueFollows"));
    }
} 