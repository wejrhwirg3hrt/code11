package org.example.repository;

import org.example.entity.DailyCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCheckinRepository extends JpaRepository<DailyCheckin, Long> {
    
    /**
     * 检查用户今日是否已签到
     */
    Optional<DailyCheckin> findByUserIdAndCheckinDate(Long userId, LocalDate date);
    
    /**
     * 获取用户最近的签到记录
     */
    Optional<DailyCheckin> findTopByUserIdOrderByCheckinDateDesc(Long userId);
    
    /**
     * 获取用户的签到历史
     */
    List<DailyCheckin> findByUserIdOrderByCheckinDateDesc(Long userId);
    
    /**
     * 统计用户总签到天数
     */
    @Query("SELECT COUNT(dc) FROM DailyCheckin dc WHERE dc.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户在指定日期范围内的签到记录
     */
    @Query("SELECT dc FROM DailyCheckin dc WHERE dc.userId = :userId AND dc.checkinDate BETWEEN :startDate AND :endDate ORDER BY dc.checkinDate DESC")
    List<DailyCheckin> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
}
