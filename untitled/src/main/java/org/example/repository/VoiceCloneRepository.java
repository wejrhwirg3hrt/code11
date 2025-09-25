package org.example.repository;

import org.example.entity.VoiceClone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceCloneRepository extends JpaRepository<VoiceClone, Long> {
    
    // 根据用户ID查找语音模型
    List<VoiceClone> findByUserIdOrderByCreatedTimeDesc(Long userId);
    
    // 根据用户ID和模型ID查找
    Optional<VoiceClone> findByIdAndUserId(Long id, Long userId);
    
    // 查找公开的语音模型
    @Query("SELECT v FROM VoiceClone v WHERE v.isPublic = true ORDER BY v.createdTime DESC")
    List<VoiceClone> findPublicVoiceClones();
    
    // 统计用户的语音模型数量
    long countByUserId(Long userId);
    
    // 根据语音名称搜索
    @Query("SELECT v FROM VoiceClone v WHERE v.voiceName LIKE %:keyword% AND (v.isPublic = true OR v.userId = :userId)")
    List<VoiceClone> searchByVoiceName(String keyword, Long userId);
}
