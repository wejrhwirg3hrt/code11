package org.example.repository;

import org.example.entity.DailyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {
    
    /**
     * 获取所有活跃的每日任务
     */
    List<DailyTask> findByActiveTrue();
    
    /**
     * 根据任务类型获取任务
     */
    List<DailyTask> findByTypeAndActiveTrue(String type);
    
    /**
     * 获取所有任务类型
     */
    @Query("SELECT DISTINCT d.type FROM DailyTask d WHERE d.active = true")
    List<String> findAllActiveTaskTypes();
}
