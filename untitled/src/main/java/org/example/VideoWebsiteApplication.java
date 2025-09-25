package org.example;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.example.service.UserService;
import org.example.service.AdminUserService;
import org.example.config.DataInitializer;
import org.example.service.DataInitializationService;
import org.example.util.DatabaseFixUtil;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableScheduling
public class VideoWebsiteApplication {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private DataInitializationService dataInitializationService;

    @Autowired
    private DatabaseFixUtil databaseFixUtil;

    public static void main(String[] args) {
        SpringApplication.run(VideoWebsiteApplication.class, args);
    }

    @PostConstruct
    public void initDefaultUsers() {
        try {
            System.out.println("=== 🚀 开始基础数据初始化 ===");

            // 检查并修复重复用户ID问题
            try {
                System.out.println("🔍 检查数据库状态...");
                databaseFixUtil.checkDatabaseStatus();
                
                System.out.println("🔧 修复重复用户ID...");
                databaseFixUtil.fixDuplicateUserIds();
            } catch (Exception e) {
                System.err.println("⚠️ 数据库检查和修复过程中出现错误: " + e.getMessage());
                System.err.println("继续执行其他初始化步骤...");
            }

            // 初始化基础数据（分类、标签、成就等）
            try {
                dataInitializationService.initializeBasicData();
            } catch (Exception e) {
                System.err.println("⚠️ 基础数据初始化过程中出现错误: " + e.getMessage());
                System.err.println("继续执行其他初始化步骤...");
            }

            // 创建默认管理员用户
            try {
                if (!userService.existsByUsername("admin")) {
                    userService.createDefaultAdmin();
                    System.out.println("✅ 默认管理员用户创建完成");
                } else {
                    System.out.println("ℹ️ 默认管理员用户已存在");
                }
            } catch (Exception e) {
                System.err.println("⚠️ 管理员用户创建过程中出现错误: " + e.getMessage());
                System.err.println("继续执行其他初始化步骤...");
            }

            // 创建默认超级管理员
            try {
                adminUserService.createDefaultSuperAdmin();
                System.out.println("✅ 超级管理员初始化完成");
            } catch (Exception e) {
                System.err.println("⚠️ 超级管理员初始化过程中出现错误: " + e.getMessage());
                System.err.println("继续执行其他初始化步骤...");
            }

            System.out.println("=== ✅ 基础数据初始化完成 ===");
            System.out.println("💡 如需创建测试用户，请在后台管理界面点击'初始化测试数据'按钮");
        } catch (Exception e) {
            System.err.println("❌ 数据初始化失败: " + e.getMessage());
            e.printStackTrace();
            // 不抛出异常，让应用继续启动
        }
    }
}