package org.example.service;

import org.example.entity.User;
import org.example.entity.Category;
import org.example.entity.DailyTask;
import org.example.repository.UserRepository;
import org.example.repository.CategoryRepository;
import org.example.repository.DailyTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class DataInitializationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DailyTaskRepository dailyTaskRepository;

    @Autowired
    private DataBackupService dataBackupService;

    // 移除 run 方法，改为手动调用的方法
    @Transactional
    public void initializeBasicData() {
        System.out.println("🚀 开始初始化基础数据...");
        
        // 初始化默认分类
        initializeCategories();
        
        // 初始化每日任务
        initializeDailyTasks();
        
        // 创建数据备份
        dataBackupService.createBackup();
        
        System.out.println("✅ 基础数据初始化完成！");
    }

    /**
     * 初始化默认分类
     */
    @Transactional
    private void initializeCategories() {
        List<String> defaultCategories = Arrays.asList(
            "音乐", "游戏", "教育", "娱乐", "科技", "生活", "体育", "电影", "动画", "其他"
        );

        for (String categoryName : defaultCategories) {
            if (!categoryRepository.existsByName(categoryName)) {
                Category category = new Category();
                category.setName(categoryName);
                category.setDescription(categoryName + "分类");
                category.setCreatedAt(LocalDateTime.now());
                categoryRepository.save(category);
                System.out.println("✅ 创建分类: " + categoryName);
            }
        }
    }

    /**
     * 初始化默认每日任务
     */
    @Transactional
    private void initializeDailyTasks() {
        if (dailyTaskRepository.count() == 0) {
            List<DailyTask> defaultTasks = Arrays.asList(
                new DailyTask("上传视频", "今日上传1个视频", "video_upload", 1, 50),
                new DailyTask("观看视频", "观看3个视频", "video_watch", 3, 20),
                new DailyTask("发表评论", "发表5条评论", "comment", 5, 30),
                new DailyTask("点赞互动", "点赞10个视频", "like", 10, 25),
                new DailyTask("分享内容", "分享2个视频", "share", 2, 35)
            );
            
            dailyTaskRepository.saveAll(defaultTasks);
            System.out.println("✅ 初始化默认每日任务完成");
        } else {
            System.out.println("ℹ️ 每日任务已存在，跳过初始化");
        }
    }

    /**
     * 初始化默认用户（保留此方法供手动调用）
     */
    @Transactional
    public void initializeDefaultUsers() {
        // 检查是否已存在管理员用户（检查用户名和邮箱）
        if (!userRepository.existsByUsername("admin") && !userRepository.existsByEmail("admin@example.com")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa"); // admin
            admin.setNickname("超级管理员");
            admin.setRole("SUPER_ADMIN");
            admin.setEnabled(true);
            admin.setBanned(false);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin.setAvatar("/images/default-avatar.png");
            userRepository.save(admin);
            System.out.println("✅ 创建超级管理员用户: admin");
        } else {
            System.out.println("ℹ️ 超级管理员用户已存在，跳过创建");
        }

        // 检查是否已存在测试用户（检查用户名和邮箱）
        if (!userRepository.existsByUsername("test") && !userRepository.existsByEmail("test@example.com")) {
            User testUser = new User();
            testUser.setUsername("test");
            testUser.setEmail("test@example.com");
            testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa"); // test
            testUser.setNickname("测试用户");
            testUser.setRole("USER");
            testUser.setEnabled(true);
            testUser.setBanned(false);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());
            testUser.setAvatar("/images/default-avatar.png");
            userRepository.save(testUser);
            System.out.println("✅ 创建测试用户: test");
        } else {
            System.out.println("ℹ️ 测试用户已存在，跳过创建");
        }
    }
} 