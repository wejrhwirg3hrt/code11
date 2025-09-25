package org.example.service;

import org.example.entity.DailyTask;
import org.example.repository.DailyTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据库修复服务
 * 用于修复数据库中的各种问题
 */
@Service
@Transactional
public class DatabaseFixService {

    @Autowired
    private DailyTaskRepository dailyTaskRepository;

    /**
     * 修复daily_tasks表中的无效日期时间值
     */
    @Transactional(readOnly = false)
    public void fixDailyTasksDateTime() {
        System.out.println("🔧 开始修复daily_tasks表的日期时间值...");
        
        try {
            // 获取所有daily_tasks记录
            List<DailyTask> allTasks = dailyTaskRepository.findAll();
            int fixedCount = 0;
            
            for (DailyTask task : allTasks) {
                boolean needsUpdate = false;
                
                // 检查createdAt
                if (task.getCreatedAt() == null) {
                    // 使用Hibernate的自动时间戳功能
                    dailyTaskRepository.save(task);
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    fixedCount++;
                }
            }
            
            System.out.println("✅ 修复完成！共修复了 " + fixedCount + " 条记录");
            
        } catch (Exception e) {
            System.err.println("❌ 修复daily_tasks表时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查并修复所有数据库问题
     */
    public void fixAllDatabaseIssues() {
        System.out.println("🔧 开始检查和修复数据库问题...");
        
        // 修复daily_tasks表的日期时间值
        fixDailyTasksDateTime();
        
        System.out.println("✅ 数据库问题修复完成！");
    }
} 