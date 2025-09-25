package org.example.repository;

import org.example.entity.User;
import org.example.entity.UserLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户等级数据访问层
 */
@Repository
public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {

    /**
     * 根据用户查找等级信息
     */
    Optional<UserLevel> findByUser(User user);

    /**
     * 根据用户ID查找等级信息
     */
    @Query("SELECT ul FROM UserLevel ul WHERE ul.user.id = :userId")
    Optional<UserLevel> findByUserId(Long userId);

    /**
     * 获取等级排行榜
     */
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.level DESC, ul.experiencePoints DESC")
    List<UserLevel> findTopByOrderByLevelDescExperiencePointsDesc(Pageable pageable);

    /**
     * 根据等级范围查找用户
     */
    @Query("SELECT ul FROM UserLevel ul WHERE ul.level BETWEEN :minLevel AND :maxLevel ORDER BY ul.level DESC, ul.experiencePoints DESC")
    List<UserLevel> findByLevelBetween(Integer minLevel, Integer maxLevel, Pageable pageable);

    /**
     * 获取指定等级以上的用户数量
     */
    @Query("SELECT COUNT(ul) FROM UserLevel ul WHERE ul.level >= :level")
    Long countByLevelGreaterThanEqual(Integer level);

    /**
     * 获取平均等级
     */
    @Query("SELECT AVG(ul.level) FROM UserLevel ul")
    Double getAverageLevel();

    /**
     * 获取平均经验值
     */
    @Query("SELECT AVG(ul.experiencePoints) FROM UserLevel ul")
    Double getAverageExperiencePoints();

    /**
     * 查找最近更新的用户等级
     */
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.updatedAt DESC")
    List<UserLevel> findRecentlyUpdated(Pageable pageable);

    /**
     * 查找最近获得经验的用户
     */
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.lastExpGain DESC")
    List<UserLevel> findRecentExpGainers(Pageable pageable);

    /**
     * 根据连续登录天数排序
     */
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.consecutiveDays DESC")
    List<UserLevel> findByConsecutiveDaysDesc(Pageable pageable);

    /**
     * 根据上传视频数量排序
     */
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.totalVideosUploaded DESC")
    List<UserLevel> findByTotalVideosUploadedDesc(Pageable pageable);

    /**
     * 根据获得点赞数排序
     */
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.totalLikesReceived DESC")
    List<UserLevel> findByTotalLikesReceivedDesc(Pageable pageable);

    /**
     * 根据观看时长排序
     */
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.totalWatchTime DESC")
    List<UserLevel> findByTotalWatchTimeDesc(Pageable pageable);
    
    /**
     * 删除用户的所有等级数据
     */
    void deleteByUserId(Long userId);
}
