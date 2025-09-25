package org.example.repository;

import java.util.List;

import org.example.entity.UserMoment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMomentRepository extends JpaRepository<UserMoment, Long> {
    
    // 根据用户ID查找动态（预加载user对象）
    @Query("SELECT m FROM UserMoment m JOIN FETCH m.user WHERE m.user.id = :userId ORDER BY m.createTime DESC")
    Page<UserMoment> findByUser_IdOrderByCreateTimeDesc(@Param("userId") Long userId, Pageable pageable);
    
    // 查找公开的动态（预加载user对象）
    @Query("SELECT m FROM UserMoment m JOIN FETCH m.user WHERE m.isPublic = true ORDER BY m.createTime DESC")
    Page<UserMoment> findByIsPublicTrueOrderByCreateTimeDesc(Pageable pageable);

    // 查找关注用户的动态（预加载user对象）
    @Query("SELECT m FROM UserMoment m JOIN FETCH m.user WHERE m.user.id IN :userIds AND m.isPublic = true ORDER BY m.createTime DESC")
    Page<UserMoment> findByUserIdInAndIsPublicTrueOrderByCreateTimeDesc(@Param("userIds") List<Long> userIds, Pageable pageable);
    
    // 根据ID查找动态（预加载user对象）
    @Query("SELECT m FROM UserMoment m JOIN FETCH m.user WHERE m.id = :id")
    UserMoment findByIdWithUser(@Param("id") Long id);
    
    // 统计用户的动态数量
    long countByUser_Id(Long userId);
    
    // 根据内容搜索动态（预加载user对象）
    @Query("SELECT m FROM UserMoment m JOIN FETCH m.user WHERE m.content LIKE %:keyword% AND m.isPublic = true ORDER BY m.createTime DESC")
    List<UserMoment> searchByContent(@Param("keyword") String keyword);
    
    // 删除用户的所有动态
    void deleteByUser_Id(Long userId);
}
