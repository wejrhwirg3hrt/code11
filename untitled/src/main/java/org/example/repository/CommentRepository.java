package org.example.repository;

import org.example.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByVideoId(Long videoId);

    List<Comment> findByUserId(Long userId);

    List<Comment> findByStatus(Comment.CommentStatus status);

    Page<Comment> findByStatus(Comment.CommentStatus status, Pageable pageable);

    long countByStatus(Comment.CommentStatus status);

    List<Comment> findByParentId(Long parentId);
    // 查询最新评论
    List<Comment> findTop10ByOrderByCreatedAtDesc();

    // 统计用户评论数量
    long countByUserId(Long userId);

    // 按时间范围统计视频评论数
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.videoId = :videoId AND c.createdAt BETWEEN :startDate AND :endDate")
    long countByVideoIdAndCreatedAtBetween(@Param("videoId") Long videoId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 统计单个视频的评论数
    long countByVideoId(Long videoId);

    /**
     * 删除视频的所有评论
     */
    void deleteByVideoId(Long videoId);

    /**
     * 删除用户的所有评论
     */
    void deleteByUserId(Long userId);

}