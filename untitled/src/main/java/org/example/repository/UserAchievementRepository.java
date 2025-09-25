package org.example.repository;

import org.example.entity.Achievement;
import org.example.entity.User;
import org.example.entity.UserAchievement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户成就关联数据访问层
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    /**
     * 根据用户获取所有成就，按解锁时间倒序
     */
    List<UserAchievement> findByUserOrderByUnlockedAtDesc(User user);

    /**
     * 根据用户获取所有成就，分页
     */
    List<UserAchievement> findByUserOrderByUnlockedAtDesc(User user, Pageable pageable);

    /**
     * 根据用户获取显示的成就
     */
    List<UserAchievement> findByUserAndIsDisplayedTrueOrderByUnlockedAtDesc(User user);

    /**
     * 根据用户和成就查找记录
     */
    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);

    /**
     * 检查用户是否已解锁指定成就
     */
    boolean existsByUserAndAchievement(User user, Achievement achievement);

    /**
     * 获取用户已解锁的成就数量
     */
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user")
    Long countByUser(@Param("user") User user);

    /**
     * 获取用户在指定分类下的成就数量
     */
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user AND ua.achievement.category = :category")
    Long countByUserAndAchievementCategory(@Param("user") User user, @Param("category") Achievement.AchievementCategory category);

    /**
     * 获取用户在指定稀有度下的成就数量
     */
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user AND ua.achievement.rarity = :rarity")
    Long countByUserAndAchievementRarity(@Param("user") User user, @Param("rarity") Achievement.AchievementRarity rarity);

    /**
     * 获取用户获得的总成就点数
     */
    @Query("SELECT SUM(ua.achievement.points) FROM UserAchievement ua WHERE ua.user = :user")
    Long getTotalPointsByUser(@Param("user") User user);

    /**
     * 获取用户最近解锁的成就
     */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findRecentByUser(@Param("user") User user, Pageable pageable);

    /**
     * 获取指定时间段内解锁的成就
     */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.unlockedAt BETWEEN :startDate AND :endDate ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findByUserAndUnlockedAtBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 获取用户在指定分类下的成就
     */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.achievement.category = :category ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findByUserAndAchievementCategory(@Param("user") User user, @Param("category") Achievement.AchievementCategory category);

    /**
     * 获取用户在指定稀有度下的成就
     */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.achievement.rarity = :rarity ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findByUserAndAchievementRarity(@Param("user") User user, @Param("rarity") Achievement.AchievementRarity rarity);

    /**
     * 获取用户显示的成就数量
     */
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user AND ua.isDisplayed = true")
    Long countByUserAndIsDisplayedTrue(@Param("user") User user);

    /**
     * 获取所有用户的成就统计（用于排行榜）
     */
    @Query("SELECT ua.user, COUNT(ua), SUM(ua.achievement.points) FROM UserAchievement ua GROUP BY ua.user ORDER BY COUNT(ua) DESC, SUM(ua.achievement.points) DESC")
    List<Object[]> getAchievementLeaderboard(Pageable pageable);

    /**
     * 获取指定成就的解锁用户数量
     */
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.achievement = :achievement")
    Long countByAchievement(@Param("achievement") Achievement achievement);

    /**
     * 获取最近解锁指定成就的用户
     */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.achievement = :achievement ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findRecentByAchievement(@Param("achievement") Achievement achievement, Pageable pageable);

    /**
     * 删除用户的所有成就记录
     */
    void deleteByUser(User user);

    /**
     * 获取用户成就完成率
     */
    @Query("SELECT (COUNT(ua) * 100.0 / (SELECT COUNT(a) FROM Achievement a WHERE a.isActive = true)) FROM UserAchievement ua WHERE ua.user = :user")
    Double getCompletionRateByUser(@Param("user") User user);

    // ==================== 排行榜相关查询 ====================

    /**
     * 获取用户成就统计排行榜
     */
    @Query("SELECT u.id, u.username, COUNT(ua), COALESCE(SUM(a.points), 0), " +
           "(COUNT(ua) * 100.0 / (SELECT COUNT(a2) FROM Achievement a2 WHERE a2.isActive = true)), " +
           "MAX(a.name), MAX(ua.unlockedAt) " +
           "FROM User u LEFT JOIN UserAchievement ua ON u.id = ua.user.id " +
           "LEFT JOIN Achievement a ON ua.achievement.id = a.id " +
           "GROUP BY u.id, u.username " +
           "ORDER BY COUNT(ua) DESC, SUM(a.points) DESC")
    List<Object[]> getUserAchievementStats(Pageable pageable);

    /**
     * 获取分类成就排行榜
     */
    @Query("SELECT u.id, u.username, COUNT(ua), COALESCE(SUM(a.points), 0) " +
           "FROM User u LEFT JOIN UserAchievement ua ON u.id = ua.user.id " +
           "LEFT JOIN Achievement a ON ua.achievement.id = a.id " +
           "WHERE a.category = :category " +
           "GROUP BY u.id, u.username " +
           "ORDER BY COUNT(ua) DESC, SUM(a.points) DESC")
    List<Object[]> getCategoryAchievementStats(@Param("category") String category, Pageable pageable);

    /**
     * 获取最近解锁的成就
     */
    @Query("SELECT u.id, u.username, a.id, a.name, a.icon, a.points, ua.unlockedAt " +
           "FROM UserAchievement ua " +
           "JOIN ua.user u " +
           "JOIN ua.achievement a " +
           "WHERE ua.unlockedAt >= :since " +
           "ORDER BY ua.unlockedAt DESC")
    List<Object[]> getRecentAchievements(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 获取最近N天解锁的成就
     */
    default List<Object[]> getRecentAchievements(int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return getRecentAchievements(since, pageable);
    }

    /**
     * 获取用户排名
     */
    @Query("SELECT COUNT(DISTINCT u2.id) + 1 " +
           "FROM User u2 " +
           "LEFT JOIN UserAchievement ua2 ON u2.id = ua2.user.id " +
           "WHERE (SELECT COUNT(ua1) FROM UserAchievement ua1 WHERE ua1.user.id = u2.id) > " +
           "(SELECT COUNT(ua3) FROM UserAchievement ua3 WHERE ua3.user.id = :userId)")
    Integer getUserRank(@Param("userId") Long userId);

    /**
     * 获取用户总积分
     */
    @Query("SELECT COALESCE(SUM(a.points), 0) FROM UserAchievement ua JOIN ua.achievement a WHERE ua.user.id = :userId")
    Long getUserTotalPoints(@Param("userId") Long userId);

    /**
     * 根据用户ID获取成就列表
     */
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已获得指定成就
     */
    @Query("SELECT COUNT(ua) > 0 FROM UserAchievement ua WHERE ua.user.id = :userId AND ua.achievement.id = :achievementId")
    boolean existsByUserIdAndAchievementId(@Param("userId") Long userId, @Param("achievementId") Long achievementId);
}
