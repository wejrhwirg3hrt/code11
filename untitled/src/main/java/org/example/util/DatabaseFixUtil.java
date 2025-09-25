package org.example.util;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DatabaseFixUtil {

    @Autowired
    private UserRepository userRepository;

    /**
     * 检查并修复重复的用户ID
     */
    @Transactional
    public void fixDuplicateUserIds() {
        System.out.println("🔍 开始检查重复用户ID...");
        
        // 获取所有用户
        List<User> allUsers = userRepository.findAll();
        
        // 按ID分组，找出重复的ID
        Map<Long, List<User>> usersById = allUsers.stream()
                .collect(Collectors.groupingBy(User::getId));
        
        // 找出有重复ID的用户组
        Map<Long, List<User>> duplicateGroups = usersById.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        if (duplicateGroups.isEmpty()) {
            System.out.println("✅ 没有发现重复的用户ID");
            return;
        }
        
        System.out.println("⚠️  发现 " + duplicateGroups.size() + " 个重复的用户ID:");
        
        // 处理每个重复的ID组
        for (Map.Entry<Long, List<User>> entry : duplicateGroups.entrySet()) {
            Long duplicateId = entry.getKey();
            List<User> duplicateUsers = entry.getValue();
            
            System.out.println("   ID " + duplicateId + " 有 " + duplicateUsers.size() + " 个重复记录:");
            
            // 按创建时间排序，保留最早创建的
            duplicateUsers.sort((u1, u2) -> {
                if (u1.getCreatedAt() == null && u2.getCreatedAt() == null) return 0;
                if (u1.getCreatedAt() == null) return 1;
                if (u2.getCreatedAt() == null) return -1;
                return u1.getCreatedAt().compareTo(u2.getCreatedAt());
            });
            
            // 保留第一个（最早创建的），删除其他的
            User userToKeep = duplicateUsers.get(0);
            List<User> usersToDelete = duplicateUsers.subList(1, duplicateUsers.size());
            
            System.out.println("   ✅ 保留: " + userToKeep.getUsername() + " (创建时间: " + userToKeep.getCreatedAt() + ")");
            
            for (User userToDelete : usersToDelete) {
                System.out.println("   ❌ 删除: " + userToDelete.getUsername() + " (创建时间: " + userToDelete.getCreatedAt() + ")");
                userRepository.delete(userToDelete);
            }
        }
        
        System.out.println("✅ 重复用户ID修复完成!");
    }
    
    /**
     * 检查数据库状态
     */
    @Transactional(readOnly = true)
    public void checkDatabaseStatus() {
        System.out.println("📊 数据库状态检查:");
        
        long totalUsers = userRepository.count();
        System.out.println("   总用户数: " + totalUsers);
        
        // 检查重复ID
        List<User> allUsers = userRepository.findAll();
        Map<Long, Long> idCounts = allUsers.stream()
                .collect(Collectors.groupingBy(User::getId, Collectors.counting()));
        
        long duplicateIds = idCounts.values().stream()
                .filter(count -> count > 1)
                .count();
        
        System.out.println("   重复ID数量: " + duplicateIds);
        
        if (duplicateIds > 0) {
            System.out.println("⚠️  发现重复ID，建议运行修复工具");
        } else {
            System.out.println("✅ 没有重复ID");
        }
    }
} 