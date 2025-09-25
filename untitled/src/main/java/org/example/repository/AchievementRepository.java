package org.example.repository;

import org.example.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 成就数据访问层
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    /**
     * 获取所有启用的成就，按分类和稀有度排序
     */
    @Query("SELECT a FROM Achievement a WHERE a.isActive = true ORDER BY a.category ASC, a.rarity ASC")
    List<Achievement> findAllActiveOrderByCategoryAscRarityAsc();

    /**
     * 获取所有成就，按分类和稀有度排序
     */
    @Query("SELECT a FROM Achievement a ORDER BY a.category ASC, a.rarity ASC")
    List<Achievement> findAllByOrderByCategoryAscRarityAsc();

    /**
     * 根据分类获取成就
     */
    @Query("SELECT a FROM Achievement a WHERE a.category = :category AND a.isActive = true ORDER BY a.rarity ASC")
    List<Achievement> findByCategoryAndIsActiveTrue(@Param("category") Achievement.AchievementCategory category);

    /**
     * 根据稀有度获取成就
     */
    @Query("SELECT a FROM Achievement a WHERE a.rarity = :rarity AND a.isActive = true ORDER BY a.category ASC")
    List<Achievement> findByRarityAndIsActiveTrue(@Param("rarity") Achievement.AchievementRarity rarity);

    /**
     * 根据名称查找成就
     */
    Optional<Achievement> findByNameAndIsActiveTrue(String name);

    /**
     * 搜索成就（按名称或描述）
     */
    @Query("SELECT a FROM Achievement a WHERE a.isActive = true AND (LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY a.category ASC, a.rarity ASC")
    List<Achievement> searchAchievements(@Param("keyword") String keyword);

    /**
     * 获取指定分类的成就数量
     */
    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.category = :category AND a.isActive = true")
    Long countByCategoryAndIsActiveTrue(@Param("category") Achievement.AchievementCategory category);

    /**
     * 获取指定稀有度的成就数量
     */
    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.rarity = :rarity AND a.isActive = true")
    Long countByRarityAndIsActiveTrue(@Param("rarity") Achievement.AchievementRarity rarity);

    /**
     * 获取总成就点数
     */
    @Query("SELECT SUM(a.points) FROM Achievement a WHERE a.isActive = true")
    Long getTotalPoints();

    /**
     * 根据点数范围查找成就
     */
    @Query("SELECT a FROM Achievement a WHERE a.points BETWEEN :minPoints AND :maxPoints AND a.isActive = true ORDER BY a.points DESC")
    List<Achievement> findByPointsBetweenAndIsActiveTrue(@Param("minPoints") Integer minPoints, @Param("maxPoints") Integer maxPoints);

    /**
     * 获取最有价值的成就（按点数排序）
     */
    @Query("SELECT a FROM Achievement a WHERE a.isActive = true ORDER BY a.points DESC")
    List<Achievement> findTopByPointsDesc();

    /**
     * 根据创建时间获取最新成就
     */
    @Query("SELECT a FROM Achievement a WHERE a.isActive = true ORDER BY a.createdAt DESC")
    List<Achievement> findRecentAchievements();

    /**
     * 检查成就名称是否已存在
     */
    boolean existsByNameAndIsActiveTrue(String name);

    /**
     * 获取所有启用的成就
     */
    List<Achievement> findByIsActiveTrue();
}
