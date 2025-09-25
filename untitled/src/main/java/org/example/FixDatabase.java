package org.example;

import org.example.util.DatabaseFixUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FixDatabase {

    public static void main(String[] args) {
        System.out.println("🔧 启动数据库修复工具...");
        
        ConfigurableApplicationContext context = SpringApplication.run(FixDatabase.class, args);
        
        try {
            DatabaseFixUtil databaseFixUtil = context.getBean(DatabaseFixUtil.class);
            
            // 先检查状态
            databaseFixUtil.checkDatabaseStatus();
            
            // 执行修复
            databaseFixUtil.fixDuplicateUserIds();
            
            // 再次检查状态
            System.out.println("\n🔍 修复后状态检查:");
            databaseFixUtil.checkDatabaseStatus();
            
            System.out.println("✅ 数据库修复完成!");
            
        } catch (Exception e) {
            System.err.println("❌ 修复过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
} 