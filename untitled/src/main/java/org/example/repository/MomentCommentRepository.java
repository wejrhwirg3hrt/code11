package org.example.repository;

import org.example.entity.MomentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MomentCommentRepository extends JpaRepository<MomentComment, Long> {
    
    // 根据动态ID查找评论
    List<MomentComment> findByMomentIdOrderByCreateTimeAsc(Long momentId);
    
    // 根据动态ID查找顶级评论（非回复）
    List<MomentComment> findByMomentIdAndReplyToCommentIdIsNullOrderByCreateTimeAsc(Long momentId);
    
    // 根据回复的评论ID查找回复
    List<MomentComment> findByReplyToCommentIdOrderByCreateTimeAsc(Long replyToCommentId);
    
    // 统计动态的评论数量
    long countByMomentId(Long momentId);
    
    // 根据用户ID查找评论
    List<MomentComment> findByUserIdOrderByCreateTimeDesc(Long userId);
}
