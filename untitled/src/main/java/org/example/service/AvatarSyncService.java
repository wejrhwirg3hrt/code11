package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.util.AvatarUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 头像同步服务
 * 统一管理所有用户的头像显示和更新
 */
@Service
public class AvatarSyncService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 同步所有用户的头像到默认头像
     * 修复无效的头像路径
     */
    public void syncAllAvatarsToDefault() {
        List<User> allUsers = userRepository.findAll();
        int fixedCount = 0;
        
        for (User user : allUsers) {
            if (AvatarUtil.isInvalidAvatarPath(user.getAvatar())) {
                user.setAvatar(AvatarUtil.DEFAULT_AVATAR_PATH);
                userRepository.save(user);
                fixedCount++;
                System.out.println("✅ 修复用户 " + user.getUsername() + " 的头像路径");
            }
        }
        
        System.out.println("🎯 头像同步完成，修复了 " + fixedCount + " 个用户的头像");
    }

    /**
     * 同步指定用户的头像
     * 
     * @param userId 用户ID
     * @return 是否成功同步
     */
    public boolean syncUserAvatar(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            System.err.println("❌ 用户不存在: " + userId);
            return false;
        }
        
        User user = userOpt.get();
        if (AvatarUtil.isInvalidAvatarPath(user.getAvatar())) {
            user.setAvatar(AvatarUtil.DEFAULT_AVATAR_PATH);
            userRepository.save(user);
            System.out.println("✅ 修复用户 " + user.getUsername() + " 的头像路径");
            return true;
        }
        
        return false;
    }

    /**
     * 获取用户的安全头像URL
     * 
     * @param userId 用户ID
     * @return 安全的头像URL
     */
    public String getSafeAvatarUrl(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return AvatarUtil.DEFAULT_AVATAR_PATH;
        }
        
        User user = userOpt.get();
        return AvatarUtil.getSafeAvatarUrl(user.getAvatar());
    }

    /**
     * 检查并修复无效的头像路径
     * 
     * @param avatarPath 头像路径
     * @return 修复后的头像路径
     */
    public String fixAvatarPath(String avatarPath) {
        return AvatarUtil.getSafeAvatarUrl(avatarPath);
    }

    /**
     * 批量修复头像路径
     * 
     * @param limit 限制修复数量
     * @return 修复的用户数量
     */
    public int batchFixAvatarPaths(int limit) {
        List<User> users = userRepository.findAll();
        int fixedCount = 0;
        
        for (User user : users) {
            if (fixedCount >= limit) {
                break;
            }
            
            if (AvatarUtil.isInvalidAvatarPath(user.getAvatar())) {
                user.setAvatar(AvatarUtil.DEFAULT_AVATAR_PATH);
                userRepository.save(user);
                fixedCount++;
                System.out.println("✅ 批量修复: 用户 " + user.getUsername() + " 的头像路径");
            }
        }
        
        return fixedCount;
    }

    /**
     * 获取头像统计信息
     * 
     * @return 统计信息
     */
    public String getAvatarStatistics() {
        List<User> allUsers = userRepository.findAll();
        int totalUsers = allUsers.size();
        int defaultAvatarUsers = 0;
        int invalidAvatarUsers = 0;
        int customAvatarUsers = 0;
        
        for (User user : allUsers) {
            String avatar = user.getAvatar();
            if (AvatarUtil.isDefaultAvatar(avatar)) {
                defaultAvatarUsers++;
            } else if (AvatarUtil.isInvalidAvatarPath(avatar)) {
                invalidAvatarUsers++;
            } else {
                customAvatarUsers++;
            }
        }
        
        return String.format(
            "📊 头像统计:\n" +
            "- 总用户数: %d\n" +
            "- 使用默认头像: %d\n" +
            "- 无效头像路径: %d\n" +
            "- 自定义头像: %d",
            totalUsers, defaultAvatarUsers, invalidAvatarUsers, customAvatarUsers
        );
    }
} 