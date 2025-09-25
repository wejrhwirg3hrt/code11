package org.example.repository;

import org.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // 查找未删除的用户
    @Query("SELECT u FROM User u WHERE u.deleted = false")
    List<User> findAllActiveUsers();

    // 根据用户名查找未删除的用户
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.deleted = false")
    Optional<User> findActiveByUsername(String username);

    /**
     * 搜索用户 - 支持用户名、邮箱、昵称搜索
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY u.username ASC")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 搜索用户（排除指定用户）
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.id != :excludeUserId AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY u.username ASC")
    List<User> searchUsersExcluding(@Param("keyword") String keyword,
                                   @Param("excludeUserId") Long excludeUserId,
                                   Pageable pageable);

    /**
     * 获取所有活跃用户（排除指定用户）
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.id != :excludeUserId ORDER BY u.username ASC")
    List<User> findAllActiveUsersExcluding(@Param("excludeUserId") Long excludeUserId);

    // 查找已删除的用户
    @Query("SELECT u FROM User u WHERE u.deleted = true")
    List<User> findAllDeletedUsers();

    // 添加查询已删除用户的方法
    List<User> findByDeletedTrue();



    // 统计活跃用户数量
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false")
    long countActiveUsers();

    // 查询活跃用户
    List<User> findByDeletedFalse();

    // 查询被封禁用户
    List<User> findByBannedTrue();

    // 查询正常用户
    List<User> findByBannedFalseAndDeletedFalse();

    // 统计启用的用户数量
    long countByEnabledTrue();

    // 统计被封禁的用户数量
    long countByBannedTrue();

    // 简单的用户名搜索方法
    List<User> findByUsernameContainingIgnoreCaseAndDeletedFalse(String username);
    
    // 根据角色查找用户
    List<User> findByRoleIn(List<String> roles);
    
    // 根据角色查找用户（排除指定角色）
    List<User> findByRoleNotIn(List<String> roles);
    
    // 统计指定角色的用户数量
    long countByRoleIn(List<String> roles);
}
