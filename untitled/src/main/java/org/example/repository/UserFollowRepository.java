package org.example.repository;

import org.example.entity.User;
import org.example.entity.UserFollow;
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
 * 用户关注关系数据访问层
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /**
     * 查找关注关系（可能有多条重复记录）
     */
    List<UserFollow> findByFollowerAndFollowing(User follower, User following);

    /**
     * 检查是否存在关注关系
     */
    @Query("SELECT COUNT(uf) > 0 FROM UserFollow uf WHERE uf.follower.id = :followerId AND uf.following.id = :followingId")
    boolean existsByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 根据关注者ID查找关注列表
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :followerId ORDER BY uf.createdAt DESC")
    Page<UserFollow> findByFollowerIdOrderByCreatedAtDesc(@Param("followerId") Long followerId, Pageable pageable);

    /**
     * 根据被关注者ID查找粉丝列表
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.following.id = :followingId ORDER BY uf.createdAt DESC")
    Page<UserFollow> findByFollowingIdOrderByCreatedAtDesc(@Param("followingId") Long followingId, Pageable pageable);

    /**
     * 根据被关注者ID查找所有粉丝
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.following.id = :followingId ORDER BY uf.createdAt DESC")
    List<UserFollow> findByFollowingId(@Param("followingId") Long followingId);

    /**
     * 统计关注数量
     */
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower.id = :followerId")
    long countByFollowerId(@Param("followerId") Long followerId);

    /**
     * 统计粉丝数量
     */
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.following.id = :followingId")
    long countByFollowingId(@Param("followingId") Long followingId);

    /**
     * 查找互相关注的用户
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :userId AND EXISTS " +
           "(SELECT uf2 FROM UserFollow uf2 WHERE uf2.follower.id = uf.following.id AND uf2.following.id = :userId)")
    List<UserFollow> findMutualFollows(@Param("userId") Long userId);

    /**
     * 获取关注建议
     * 基于共同关注的用户推荐
     */
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN " +
           "(SELECT uf2.following.id FROM UserFollow uf1 " +
           "JOIN UserFollow uf2 ON uf1.following.id = uf2.follower.id " +
           "WHERE uf1.follower.id = :userId AND uf2.following.id != :userId " +
           "AND NOT EXISTS (SELECT uf3 FROM UserFollow uf3 WHERE uf3.follower.id = :userId AND uf3.following.id = uf2.following.id))")
    List<User> findFollowSuggestions(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找活跃关注者
     * 最近有互动的关注者
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.following.id = :userId AND uf.createdAt >= :since")
    List<UserFollow> findActiveFollowers(@Param("userId") Long userId,
                                       @Param("since") LocalDateTime since,
                                       Pageable pageable);

    /**
     * 根据关注者ID删除所有关注关系
     */
    @Query("DELETE FROM UserFollow uf WHERE uf.follower.id = :followerId")
    void deleteByFollowerId(@Param("followerId") Long followerId);

    /**
     * 根据被关注者ID删除所有关注关系
     */
    @Query("DELETE FROM UserFollow uf WHERE uf.following.id = :followingId")
    void deleteByFollowingId(@Param("followingId") Long followingId);

    /**
     * 查找用户的所有关注者ID
     */
    @Query("SELECT uf.follower.id FROM UserFollow uf WHERE uf.following.id = :userId")
    List<Long> findFollowerIdsByUserId(@Param("userId") Long userId);

    /**
     * 查找用户的所有关注ID
     */
    @Query("SELECT uf.following.id FROM UserFollow uf WHERE uf.follower.id = :userId")
    List<Long> findFollowingIdsByUserId(@Param("userId") Long userId);

    /**
     * 批量检查关注关系
     */
    @Query("SELECT uf.following.id FROM UserFollow uf WHERE uf.follower.id = :followerId AND uf.following.id IN :followingIds")
    List<Long> findExistingFollows(@Param("followerId") Long followerId, @Param("followingIds") List<Long> followingIds);

    /**
     * 查找最近关注的用户
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :followerId ORDER BY uf.createdAt DESC")
    List<UserFollow> findTop10ByFollowerIdOrderByCreatedAtDesc(@Param("followerId") Long followerId, Pageable pageable);

    /**
     * 查找最近的粉丝
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.following.id = :followingId ORDER BY uf.createdAt DESC")
    List<UserFollow> findTop10ByFollowingIdOrderByCreatedAtDesc(@Param("followingId") Long followingId, Pageable pageable);

    /**
     * 统计指定时间范围内的新关注数
     */
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.following.id = :userId AND uf.createdAt BETWEEN :start AND :end")
    long countNewFollowersBetween(@Param("userId") Long userId,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    /**
     * 查找共同关注的用户
     */
    @Query("SELECT uf1.following.id FROM UserFollow uf1 " +
           "JOIN UserFollow uf2 ON uf1.following.id = uf2.following.id " +
           "WHERE uf1.follower.id = :userId1 AND uf2.follower.id = :userId2")
    List<Long> findCommonFollowings(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查找已接受的关注关系（为动态功能添加）
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :followerId")
    List<UserFollow> findByFollowerIdAndAcceptedTrue(@Param("followerId") Long followerId);

    /**
     * 根据关注者ID查找所有关注关系
     */
    List<UserFollow> findByFollowerId(Long followerId);

    /**
     * 查找重复的关注记录
     */
    @Query("SELECT uf.follower.id, uf.following.id, COUNT(*) as count FROM UserFollow uf GROUP BY uf.follower.id, uf.following.id HAVING COUNT(*) > 1")
    List<Object[]> findDuplicateFollows();

    /**
     * 根据关注者ID和被关注者ID查找关注记录
     */
    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :followerId AND uf.following.id = :followingId ORDER BY uf.createdAt ASC")
    List<UserFollow> findByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 根据关注者ID和被关注者ID删除关注关系
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserFollow uf WHERE uf.follower.id = :followerId AND uf.following.id = :followingId")
    int deleteByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}
