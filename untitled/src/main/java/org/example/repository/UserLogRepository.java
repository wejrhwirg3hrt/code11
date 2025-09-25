package org.example.repository;

import org.example.entity.UserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    // 根据用户ID查找日志
    Page<UserLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 根据用户名查找日志
    Page<UserLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    // 根据操作类型查找日志
    Page<UserLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    // 根据状态查找日志
    Page<UserLog> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    // 根据时间范围查找日志
    @Query("SELECT ul FROM UserLog ul WHERE ul.createdAt BETWEEN :startTime AND :endTime ORDER BY ul.createdAt DESC")
    Page<UserLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    // 查找最近的错误日志
    @Query("SELECT ul FROM UserLog ul WHERE ul.status = 'ERROR' ORDER BY ul.createdAt DESC")
    List<UserLog> findRecentErrors(Pageable pageable);

    // 查找特定用户的最近活动
    @Query("SELECT ul FROM UserLog ul WHERE ul.userId = :userId ORDER BY ul.createdAt DESC")
    List<UserLog> findRecentUserActivity(@Param("userId") Long userId, Pageable pageable);

    // 统计各种操作的数量
    @Query("SELECT ul.action, COUNT(ul) FROM UserLog ul GROUP BY ul.action")
    List<Object[]> countByAction();

    // 统计各种状态的数量
    @Query("SELECT ul.status, COUNT(ul) FROM UserLog ul GROUP BY ul.status")
    List<Object[]> countByStatus();
    
    // 删除用户的所有日志
    void deleteByUserId(Long userId);
    
    // 根据状态删除日志
    void deleteByStatus(String status);
    
    // 删除指定日期之前的日志
    void deleteByCreatedAtBefore(LocalDateTime date);
    
    // 根据用户名统计日志数量
    long countByUsername(String username);
    
    // 根据用户名删除日志
    void deleteByUsername(String username);
}
