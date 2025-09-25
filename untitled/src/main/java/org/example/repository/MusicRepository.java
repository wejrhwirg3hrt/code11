package org.example.repository;

import org.example.entity.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    
    // 根据用户ID查找音乐
    List<Music> findByUserIdOrderByUploadTimeDesc(Long userId);
    
    // 查找公开的音乐
    Page<Music> findByIsPublicTrueOrderByUploadTimeDesc(Pageable pageable);
    
    // 查找所有公开的音乐（不分页）
    List<Music> findByIsPublicTrueOrderByUploadTimeDesc();
    
    // 根据标题搜索
    @Query("SELECT m FROM Music m WHERE m.title LIKE %:keyword% AND m.isPublic = true")
    List<Music> searchByTitle(@Param("keyword") String keyword);
    
    // 根据艺术家搜索
    @Query("SELECT m FROM Music m WHERE m.artist LIKE %:keyword% AND m.isPublic = true")
    List<Music> searchByArtist(@Param("keyword") String keyword);
    
    // 获取热门音乐（按播放次数排序）
    @Query("SELECT m FROM Music m WHERE m.isPublic = true ORDER BY m.playCount DESC")
    List<Music> findPopularMusic(Pageable pageable);
    
    // 根据用户ID和音乐ID查找
    Optional<Music> findByIdAndUserId(Long id, Long userId);
    
    // 统计用户上传的音乐数量
    long countByUserId(Long userId);
    
    // 获取最近上传的音乐
    @Query("SELECT m FROM Music m WHERE m.isPublic = true ORDER BY m.uploadTime DESC")
    List<Music> findRecentMusic(Pageable pageable);
    
    // 统计公开音乐数量
    long countByIsPublicTrue();
    
    // 统计指定时间后的上传数量
    long countByUploadTimeAfter(LocalDateTime time);
}
