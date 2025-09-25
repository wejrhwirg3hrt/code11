package org.example.repository;

import org.example.entity.VideoLike;
import org.example.entity.User;
import org.example.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    Optional<VideoLike> findByUserAndVideo(User user, Video video);
    boolean existsByUserAndVideo(User user, Video video);
    void deleteByUserAndVideo(User user, Video video);
    long countByVideo(Video video);
    List<VideoLike> findByUserOrderByCreatedAtDesc(User user);
    
    // 根据用户ID查找点赞记录（按时间倒序）
    List<VideoLike> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 统计用户收到的总点赞数
    @Query("SELECT COUNT(vl) FROM VideoLike vl WHERE vl.video.userId = :userId")
    long countByVideoUserId(@Param("userId") Long userId);

    // 统计用户给出的总点赞数
    @Query("SELECT COUNT(vl) FROM VideoLike vl WHERE vl.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // 按时间范围统计视频点赞数
    @Query("SELECT COUNT(vl) FROM VideoLike vl WHERE vl.video.id = :videoId AND vl.createdAt BETWEEN :startDate AND :endDate")
    long countByVideoIdAndCreatedAtBetween(@Param("videoId") Long videoId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 统计单个视频的点赞数
    @Query("SELECT COUNT(vl) FROM VideoLike vl WHERE vl.video.id = :videoId")
    long countByVideoId(@Param("videoId") Long videoId);

    /**
     * 删除视频的所有点赞记录
     */
    void deleteByVideoId(Long videoId);
}