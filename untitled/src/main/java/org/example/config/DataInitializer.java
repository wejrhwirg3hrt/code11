package org.example.config;

import org.example.entity.Video;
import org.example.entity.User;
import org.example.entity.Music;
import org.example.entity.Category;
import org.example.entity.Tag;
import org.example.repository.VideoRepository;
import org.example.repository.UserRepository;
import org.example.repository.MusicRepository;
import org.example.repository.CategoryRepository;
import org.example.repository.TagRepository;

import org.example.service.AchievementService;
import org.example.service.LevelService;
import org.example.service.DatabaseFixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component  // 启用DataInitializer来初始化成就数据
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private DatabaseFixService databaseFixService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 🚀 开始基础数据初始化 ===");

        // 0. 修复数据库问题
        try {
            databaseFixService.fixAllDatabaseIssues();
            System.out.println("✅ 数据库问题修复完成");
        } catch (Exception e) {
            System.err.println("❌ 修复数据库问题时出错: " + e.getMessage());
        }

        // 1. 创建管理员用户（仅创建管理员，不创建测试用户）
        createAdminUsers();
        System.out.println("✅ 管理员用户初始化完成");

        // 2. 初始化分类数据
        initializeCategories();
        System.out.println("✅ 分类数据初始化完成");

        // 3. 初始化标签数据
        initializeTags();
        System.out.println("✅ 标签数据初始化完成");

        // 4. 初始化默认成就
        try {
            achievementService.createDefaultAchievements();
            System.out.println("✅ 成就系统初始化完成");
        } catch (Exception e) {
            System.err.println("❌ 初始化默认成就时出错: " + e.getMessage());
        }

        // 5. 初始化默认每日任务
        try {
            levelService.initializeDefaultTasks();
            System.out.println("✅ 每日任务初始化完成");
        } catch (Exception e) {
            System.err.println("❌ 初始化默认每日任务时出错: " + e.getMessage());
        }

        System.out.println("=== ✅ 基础数据初始化完成 ===");
        System.out.println("如需创建测试用户，请在后台管理界面点击'初始化测试数据'按钮");
    }

    private void createAdminUsers() {
        try {
            // 创建管理员用户
            if (!userRepository.findByUsername("admin").isPresent()) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@example.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setEnabled(true);
                adminUser.setBanned(false);
                adminUser.setRole("ADMIN");
                adminUser.setCreatedAt(LocalDateTime.now());
                adminUser.setUpdatedAt(LocalDateTime.now());
                userRepository.save(adminUser);
                System.out.println("管理员用户创建成功 - 用户名: admin, 密码: admin123");
            }

            // 创建root用户
            if (!userRepository.findByUsername("root").isPresent()) {
                User rootUser = new User();
                rootUser.setUsername("root");
                rootUser.setEmail("root@example.com");
                rootUser.setPassword(passwordEncoder.encode("root123"));
                rootUser.setEnabled(true);
                rootUser.setBanned(false);
                rootUser.setRole("ADMIN");
                rootUser.setCreatedAt(LocalDateTime.now());
                rootUser.setUpdatedAt(LocalDateTime.now());
                userRepository.save(rootUser);
                System.out.println("Root用户创建成功 - 用户名: root, 密码: root123");
            }
        } catch (Exception e) {
            System.err.println("创建管理员用户时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 初始化分类数据
     */
    private void initializeCategories() {
        try {
            String[] categoryData = {
                "娱乐,娱乐搞笑类视频",
                "教育,教育学习类视频",
                "科技,科技数码类视频",
                "音乐,音乐MV类视频",
                "游戏,游戏相关视频",
                "生活,生活日常类视频",
                "美食,美食制作类视频",
                "旅行,旅行风景类视频",
                "体育,体育运动类视频",
                "新闻,新闻资讯类视频"
            };

            for (String data : categoryData) {
                String[] parts = data.split(",");
                String name = parts[0];
                String description = parts[1];

                if (!categoryRepository.findByName(name).isPresent()) {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription(description);
                    category.setCreatedAt(LocalDateTime.now());
                    categoryRepository.save(category);
                    System.out.println("创建分类: " + name);
                }
            }
        } catch (Exception e) {
            System.err.println("初始化分类数据失败: " + e.getMessage());
        }
    }

    /**
     * 初始化标签数据
     */
    private void initializeTags() {
        try {
            String[] tagData = {
                "热门,热门内容标签",
                "推荐,推荐内容标签",
                "原创,原创内容标签",
                "搞笑,搞笑内容标签",
                "教程,教程类内容标签",
                "评测,产品评测标签",
                "Vlog,生活记录标签",
                "音乐,音乐相关标签",
                "舞蹈,舞蹈相关标签",
                "美食,美食相关标签"
            };

            for (String data : tagData) {
                String[] parts = data.split(",");
                String name = parts[0];
                String description = parts[1];

                if (!tagRepository.findByName(name).isPresent()) {
                    Tag tag = new Tag();
                    tag.setName(name);
                    tag.setDescription(description);
                    tag.setCreatedAt(LocalDateTime.now());
                    tagRepository.save(tag);
                    System.out.println("创建标签: " + name);
                }
            }
        } catch (Exception e) {
            System.err.println("初始化标签数据失败: " + e.getMessage());
        }
    }

    /**
     * 手动初始化测试数据的方法
     * 供管理员手动调用
     */
    public void initializeTestData() {
        System.out.println("=== 🚀 开始手动初始化测试数据 ===");

        try {
            // 1. 创建测试用户
            createTestUsers();
            System.out.println("✅ 测试用户数据初始化完成");

            // 2. 清理现有的样例数据
            cleanupSampleData();
            System.out.println("✅ 样例数据清理完成");

            // 3. 创建测试视频数据
            createTestVideos();
            System.out.println("✅ 测试视频数据创建完成");

            System.out.println("=== 🎉 手动测试数据初始化全部完成！===");
        } catch (Exception e) {
            System.err.println("手动初始化测试数据时出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("初始化测试数据失败: " + e.getMessage(), e);
        }
    }

    private void createTestUsers() {
        try {
            // 检查是否已存在测试用户
            if (!userRepository.findByUsername("testuser").isPresent()) {
                // 创建测试用户
                User testUser = new User();
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");
                testUser.setPassword(passwordEncoder.encode("123456"));
                testUser.setEnabled(true);
                testUser.setBanned(false);
                testUser.setCreatedAt(LocalDateTime.now());
                testUser.setUpdatedAt(LocalDateTime.now());
                userRepository.save(testUser);
                System.out.println("测试用户创建成功 - 用户名: testuser, 密码: 123456");
            }

            // 创建更多测试用户用于私信测试
            createAdditionalTestUsers();
        } catch (Exception e) {
            System.err.println("创建测试用户时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createAdditionalTestUsers() {
        try {
            String[] usernames = {"alice", "bob", "charlie", "diana", "eve"};
            String[] emails = {"alice@example.com", "bob@example.com", "charlie@example.com", "diana@example.com", "eve@example.com"};

            for (int i = 0; i < usernames.length; i++) {
                String username = usernames[i];
                String email = emails[i];

                if (!userRepository.findByUsername(username).isPresent()) {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setEnabled(true);
                    user.setBanned(false);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    System.out.println("创建测试用户 - 用户名: " + username + ", 密码: 123456");
                }
            }
        } catch (Exception e) {
            System.err.println("创建额外测试用户时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanupSampleData() {
        // 删除所有示例数据（视频、音乐等）
        try {
            // 1. 删除包含sample-videos.com的视频
            List<Video> videosWithSampleUrl = videoRepository.findByUrlContaining("sample-videos.com");
            if (!videosWithSampleUrl.isEmpty()) {
                videoRepository.deleteAll(videosWithSampleUrl);
                System.out.println("删除了 " + videosWithSampleUrl.size() + " 个包含sample-videos.com的视频");
            }

            // 2. 删除包含placeholder的缩略图的视频
            List<Video> videosWithPlaceholder = videoRepository.findByThumbnailContaining("placeholder");
            if (!videosWithPlaceholder.isEmpty()) {
                videoRepository.deleteAll(videosWithPlaceholder);
                System.out.println("删除了 " + videosWithPlaceholder.size() + " 个包含placeholder的视频");
            }

            // 3. 删除特定标题的示例视频
            String[] sampleTitles = {
                "Spring Boot教程", "Java编程入门", "Web开发实战",
                "React入门教程", "Docker容器化", "微服务架构",
                "Spring Boot 入门教程", "Java 编程基础", "Web 开发实战",
                "数据库设计", "微服务架构"
            };

            for (String title : sampleTitles) {
                List<Video> videosWithTitle = videoRepository.findByTitle(title);
                if (!videosWithTitle.isEmpty()) {
                    videoRepository.deleteAll(videosWithTitle);
                    System.out.println("删除了标题为 '" + title + "' 的 " + videosWithTitle.size() + " 个视频");
                }
            }

            // 4. 删除测试路径的视频
            List<Video> testVideos = videoRepository.findByUrlContaining("uploads/videos/test/");
            if (!testVideos.isEmpty()) {
                videoRepository.deleteAll(testVideos);
                System.out.println("删除了 " + testVideos.size() + " 个测试路径的视频");
            }

            // 5. 清理示例音乐数据（删除所有示例音乐）
            long musicCount = musicRepository.count();
            if (musicCount > 0) {
                // 删除所有音乐数据（因为都是示例数据）
                musicRepository.deleteAll();
                System.out.println("删除了 " + musicCount + " 个示例音乐");
            }

            long remainingVideos = videoRepository.count();
            long remainingMusic = musicRepository.count();
            System.out.println("示例数据清理完成，剩余视频: " + remainingVideos + ", 剩余音乐: " + remainingMusic);
        } catch (Exception e) {
            System.err.println("清理样例视频时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 创建测试视频数据
     */
    private void createTestVideos() {
        try {
            // 检查是否已有视频数据
            if (videoRepository.count() > 0) {
                System.out.println("数据库中已有视频数据，跳过创建测试视频");
                return;
            }

            // 获取测试用户
            User testUser = userRepository.findByUsername("testuser").orElse(null);
            if (testUser == null) {
                System.out.println("未找到测试用户，跳过创建测试视频");
                return;
            }

            // 获取测试分类
            Category category = categoryRepository.findByName("娱乐").orElse(null);
            if (category == null) {
                System.out.println("未找到测试分类，跳过创建测试视频");
                return;
            }

            // 创建测试视频
            String[] videoTitles = {
                "精彩搞笑视频合集",
                "美食制作教程",
                "旅行风景记录",
                "科技产品评测",
                "音乐MV欣赏"
            };

            String[] videoDescriptions = {
                "收集了最新最搞笑的视频片段，让你笑到停不下来！",
                "详细的美食制作过程，教你做出美味佳肴。",
                "记录旅行中的美好时光，分享世界各地的美景。",
                "专业的科技产品评测，帮你选择最适合的产品。",
                "精选优质音乐MV，享受视听盛宴。"
            };

            for (int i = 0; i < videoTitles.length; i++) {
                Video video = new Video();
                video.setTitle(videoTitles[i]);
                video.setDescription(videoDescriptions[i]);
                video.setFilePath("/videos/test" + (i + 1) + ".mp4");
                video.setThumbnailPath("/images/test" + (i + 1) + ".jpg");
                video.setUrl("/videos/test" + (i + 1) + ".mp4"); // 设置URL字段
                video.setThumbnail("/images/test" + (i + 1) + ".jpg"); // 设置缩略图URL
                video.setUserId(testUser.getId());
                video.setUser(testUser);
                video.setCategory(category);
                video.setStatus(Video.VideoStatus.APPROVED); // 设置为已审核状态
                video.setViews((long) (Math.random() * 1000 + 100));
                video.setLikeCount((int) (Math.random() * 100 + 10));
                video.setCreatedAt(LocalDateTime.now().minusDays(i + 1));
                video.setTagsString("测试,视频,样例,热门");
                video.setDuration("0" + (i + 3) + ":30");

                // 添加调试信息
                System.out.println("准备保存视频: " + video.getTitle());
                System.out.println("URL字段值: " + video.getUrl());
                System.out.println("用户ID: " + video.getUserId());

                videoRepository.save(video);
                System.out.println("创建测试视频: " + video.getTitle());
            }

        } catch (Exception e) {
            System.err.println("创建测试视频失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}