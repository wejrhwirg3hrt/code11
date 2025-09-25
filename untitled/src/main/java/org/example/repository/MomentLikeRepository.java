package org.example.repository;

import org.example.entity.MomentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MomentLikeRepository extends JpaRepository<MomentLike, Long> {
    
    /**
     * 查找用户对特定动态的点赞记录
     */
    Optional<MomentLike> findByMomentIdAndUserId(Long momentId, Long userId);
    
    /**
     * 检查用户是否已点赞某动态
     */
    boolean existsByMomentIdAndUserId(Long momentId, Long userId);
    
    /**
     * 获取动态的点赞数量
     */
    long countByMomentId(Long momentId);
    
    /**
     * 获取动态的所有点赞记录（包含用户信息）
     */
    @Query("SELECT ml FROM MomentLike ml WHERE ml.momentId = :momentId ORDER BY ml.createTime DESC")
    List<MomentLike> findByMomentIdOrderByCreateTimeDesc(@Param("momentId") Long momentId);

    /**
     * 获取动态的所有点赞记录（包含用户信息，限制数量）
     */
    @Query("SELECT ml FROM MomentLike ml JOIN FETCH ml.user WHERE ml.momentId = :momentId ORDER BY ml.createTime DESC")
    List<MomentLike> findByMomentIdWithUserOrderByCreateTimeDesc(@Param("momentId") Long momentId);
    
    /**
     * 删除用户对动态的点赞
     */
    void deleteByMomentIdAndUserId(Long momentId, Long userId);
}
