package org.example.repository;

import org.example.entity.UserTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTaskProgressRepository extends JpaRepository<UserTaskProgress, Long> {
    
    /**
     * 获取用户今日的任务进度
     */
    List<UserTaskProgress> findByUserIdAndDate(Long userId, LocalDate date);
    
    /**
     * 获取用户特定任务的今日进度
     */
    Optional<UserTaskProgress> findByUserIdAndTaskIdAndDate(Long userId, Long taskId, LocalDate date);
    
    /**
     * 统计用户完成的任务数量
     */
    @Query("SELECT COUNT(utp) FROM UserTaskProgress utp WHERE utp.userId = :userId AND utp.completed = true")
    long countCompletedTasksByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户在指定日期范围内完成的任务
     */
    @Query("SELECT utp FROM UserTaskProgress utp WHERE utp.userId = :userId AND utp.completed = true AND utp.date BETWEEN :startDate AND :endDate")
    List<UserTaskProgress> findCompletedTasksByUserIdAndDateRange(@Param("userId") Long userId, 
                                                                  @Param("startDate") LocalDate startDate, 
                                                                  @Param("endDate") LocalDate endDate);
}
