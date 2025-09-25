package org.example.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixRunner implements CommandLineRunner {

    @Autowired
    private DatabaseFixUtil databaseFixUtil;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否有修复参数
        if (args.length > 0 && "fix-users".equals(args[0])) {
            System.out.println("🔧 启动数据库修复工具...");
            
            // 先检查状态
            databaseFixUtil.checkDatabaseStatus();
            
            // 执行修复
            databaseFixUtil.fixDuplicateUserIds();
            
            // 再次检查状态
            System.out.println("\n🔍 修复后状态检查:");
            databaseFixUtil.checkDatabaseStatus();
            
            System.out.println("✅ 数据库修复完成，程序退出");
            System.exit(0);
        }
    }
} 