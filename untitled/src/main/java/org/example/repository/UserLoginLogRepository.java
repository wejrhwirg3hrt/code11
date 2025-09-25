package org.example.repository;

import org.example.entity.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {

    List<UserLoginLog> findByUserIdOrderByLoginTimeDesc(Long userId);

    List<UserLoginLog> findByLoginTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT l.loginLocation, COUNT(l) as count FROM UserLoginLog l WHERE l.loginLocation IS NOT NULL GROUP BY l.loginLocation")
    List<Object[]> getLoginLocationStats();

    @Query("SELECT l FROM UserLoginLog l WHERE l.latitude IS NOT NULL AND l.longitude IS NOT NULL ORDER BY l.loginTime DESC")
    List<UserLoginLog> getLoginLocations();

    long countByLoginTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // 删除用户的所有登录日志
    void deleteByUserId(Long userId);
}