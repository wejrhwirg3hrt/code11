package org.example.repository;

import org.example.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 通知数据访问层
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 根据用户ID查找通知，按创建时间倒序
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据用户ID和通知ID查找通知
     */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    /**
     * 统计用户未读通知数量
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 查找用户未读通知
     */
    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    /**
     * 查找用户指定类型的通知
     */
    List<Notification> findByUserIdAndType(Long userId, Notification.NotificationType type);

    /**
     * 查找用户指定时间范围内的通知
     */
    List<Notification> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 删除用户指定的通知
     */
    @Modifying
    @Transactional
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * 批量标记用户通知为已读
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * 删除指定时间之前的已读通知
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :beforeDate")
    void deleteReadNotificationsBefore(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 查找相关实体的通知
     */
    List<Notification> findByRelatedIdAndRelatedType(Long relatedId, String relatedType);

    /**
     * 查找来自指定用户的通知
     */
    List<Notification> findByUserIdAndFromUserId(Long userId, Long fromUserId);

    /**
     * 统计用户指定类型的未读通知数量
     */
    long countByUserIdAndTypeAndIsReadFalse(Long userId, Notification.NotificationType type);

    /**
     * 查找最近的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找系统通知
     */
    @Query("SELECT n FROM Notification n WHERE n.type = 'SYSTEM' OR n.type = 'ANNOUNCEMENT' ORDER BY n.createdAt DESC")
    List<Notification> findSystemNotifications(Pageable pageable);

    /**
     * 统计总通知数量
     */
    long countByUserId(Long userId);

    /**
     * 查找用户今日通知
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND DATE(n.createdAt) = CURRENT_DATE ORDER BY n.createdAt DESC")
    List<Notification> findTodayNotifications(@Param("userId") Long userId);

    /**
     * 查找用户本周通知
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :weekStart ORDER BY n.createdAt DESC")
    List<Notification> findWeekNotifications(@Param("userId") Long userId, @Param("weekStart") LocalDateTime weekStart);
}
