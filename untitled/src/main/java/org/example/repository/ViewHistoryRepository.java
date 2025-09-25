package org.example.repository;

import org.example.entity.User;
import org.example.entity.Video;
import org.example.entity.ViewHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 观看历史Repository
 */
@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    /**
     * 根据用户和视频查找观看历史
     */
    Optional<ViewHistory> findByUserAndVideo(User user, Video video);

    /**
     * 根据用户查找观看历史（按时间倒序）
     */
    List<ViewHistory> findByUserOrderByUpdatedAtDesc(User user);

    /**
     * 根据用户查找观看历史（分页）
     */
    Page<ViewHistory> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);

    /**
     * 获取用户最近观看的视频
     */
    @Query("SELECT vh FROM ViewHistory vh WHERE vh.user = :user ORDER BY vh.updatedAt DESC")
    List<ViewHistory> findRecentViewHistory(@Param("user") User user, Pageable pageable);

    /**
     * 获取用户观看时长最长的视频类型
     */
    @Query("SELECT v.category.name, SUM(vh.watchDuration) as totalDuration " +
           "FROM ViewHistory vh JOIN vh.video v " +
           "WHERE vh.user = :user AND v.category IS NOT NULL " +
           "GROUP BY v.category.name " +
           "ORDER BY totalDuration DESC")
    List<Object[]> findUserPreferredCategories(@Param("user") User user, Pageable pageable);

    /**
     * 获取用户观看的标签偏好
     */
    @Query("SELECT t.name, COUNT(vh) as viewCount " +
           "FROM ViewHistory vh JOIN vh.video v JOIN v.tags t " +
           "WHERE vh.user = :user " +
           "GROUP BY t.name " +
           "ORDER BY viewCount DESC")
    List<Object[]> findUserPreferredTags(@Param("user") User user, Pageable pageable);

    /**
     * 获取协同过滤推荐 - 找到观看相似视频的用户
     */
    @Query("SELECT vh2.video, COUNT(vh2) as commonViews " +
           "FROM ViewHistory vh1 JOIN ViewHistory vh2 ON vh1.video = vh2.video " +
           "WHERE vh1.user = :user AND vh2.user != :user " +
           "AND vh2.video NOT IN (SELECT vh3.video FROM ViewHistory vh3 WHERE vh3.user = :user) " +
           "GROUP BY vh2.video " +
           "ORDER BY commonViews DESC")
    List<Object[]> findCollaborativeFilteringRecommendations(@Param("user") User user, Pageable pageable);

    /**
     * 获取用户观看统计
     */
    @Query("SELECT COUNT(vh), SUM(vh.watchDuration), AVG(vh.completionRate) " +
           "FROM ViewHistory vh WHERE vh.user = :user")
    Object[] getUserViewingStats(@Param("user") User user);

    /**
     * 获取视频的观看统计
     */
    @Query("SELECT COUNT(vh), SUM(vh.watchDuration), AVG(vh.completionRate) " +
           "FROM ViewHistory vh WHERE vh.video = :video")
    Object[] getVideoViewingStats(@Param("video") Video video);

    /**
     * 获取热门视频（基于观看次数和完成率）
     */
    @Query("SELECT vh.video, COUNT(vh) as viewCount, AVG(vh.completionRate) as avgCompletion " +
           "FROM ViewHistory vh " +
           "WHERE vh.createdAt >= :startDate " +
           "GROUP BY vh.video " +
           "HAVING COUNT(vh) >= :minViews " +
           "ORDER BY viewCount DESC, avgCompletion DESC")
    List<Object[]> findTrendingVideos(@Param("startDate") LocalDateTime startDate, 
                                     @Param("minViews") Long minViews, 
                                     Pageable pageable);

    /**
     * 获取用户活跃时间分析
     */
    @Query("SELECT HOUR(vh.createdAt) as hour, COUNT(vh) as viewCount " +
           "FROM ViewHistory vh " +
           "WHERE vh.user = :user " +
           "GROUP BY HOUR(vh.createdAt) " +
           "ORDER BY viewCount DESC")
    List<Object[]> getUserActiveHours(@Param("user") User user);

    /**
     * 删除用户的观看历史
     */
    void deleteByUser(User user);

    /**
     * 删除指定时间之前的观看历史
     */
    void deleteByCreatedAtBefore(LocalDateTime date);

    /**
     * 统计用户总观看时长
     */
    @Query("SELECT SUM(vh.watchDuration) FROM ViewHistory vh WHERE vh.user = :user")
    Long getTotalWatchDuration(@Param("user") User user);

    /**
     * 获取观看趋势数据
     */
    @Query("SELECT DATE(vh.createdAt) as viewDate, COUNT(vh) as viewCount, COUNT(DISTINCT vh.user) as uniqueUsers " +
           "FROM ViewHistory vh " +
           "WHERE vh.createdAt >= :startDate " +
           "GROUP BY DATE(vh.createdAt) " +
           "ORDER BY viewDate DESC")
    List<Object[]> getViewingTrends(@Param("startDate") LocalDateTime startDate);

    /**
     * 获取设备类型统计
     */
    @Query("SELECT vh.deviceType, COUNT(vh) as count " +
           "FROM ViewHistory vh " +
           "WHERE vh.createdAt >= :startDate " +
           "GROUP BY vh.deviceType " +
           "ORDER BY count DESC")
    List<Object[]> getDeviceTypeStats(@Param("startDate") LocalDateTime startDate);

    /**
     * 统计用户观看的不同视频数量
     */
    @Query("SELECT COUNT(DISTINCT vh.video) FROM ViewHistory vh WHERE vh.user.id = :userId")
    long countDistinctVideosByUserId(@Param("userId") Long userId);

    /**
     * 统计用户总观看时长（秒）
     */
    @Query("SELECT COALESCE(SUM(vh.watchDuration), 0) FROM ViewHistory vh WHERE vh.user.id = :userId")
    long sumWatchTimeByUserId(@Param("userId") Long userId);
}
