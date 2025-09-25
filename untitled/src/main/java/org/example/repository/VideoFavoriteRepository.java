package org.example.repository;

import org.example.entity.VideoFavorite;
import org.example.entity.User;
import org.example.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoFavoriteRepository extends JpaRepository<VideoFavorite, Long> {
    Optional<VideoFavorite> findByUserAndVideo(User user, Video video);
    boolean existsByUserAndVideo(User user, Video video);
    void deleteByUserAndVideo(User user, Video video);
    List<VideoFavorite> findByUserOrderByCreatedAtDesc(User user);
    long countByVideo(Video video);

    /**
     * 统计用户收到的总收藏数
     */
    @Query("SELECT COUNT(vf) FROM VideoFavorite vf WHERE vf.video.userId = :userId")
    long countByVideoUserId(@Param("userId") Long userId);

    /**
     * 删除视频的所有收藏记录
     */
    void deleteByVideoId(Long videoId);
}