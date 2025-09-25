package org.example.repository;

import org.example.entity.SearchHistory;
import org.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 搜索历史记录Repository
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /**
     * 根据用户和搜索关键词查找记录
     */
    Optional<SearchHistory> findByUserAndSearchKeyword(User user, String searchKeyword);

    /**
     * 根据用户查找搜索历史（按更新时间倒序）
     */
    List<SearchHistory> findByUserOrderByUpdatedAtDesc(User user);

    /**
     * 根据用户查找搜索历史（分页）
     */
    Page<SearchHistory> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);

    /**
     * 获取用户最近的搜索历史
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user = :user ORDER BY sh.updatedAt DESC")
    List<SearchHistory> findRecentSearchHistory(@Param("user") User user, Pageable pageable);

    /**
     * 获取热门搜索关键词
     */
    @Query("SELECT sh.searchKeyword, SUM(sh.searchCount) as totalCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.createdAt >= :startDate " +
           "GROUP BY sh.searchKeyword " +
           "ORDER BY totalCount DESC")
    List<Object[]> findHotSearchKeywords(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * 获取搜索建议（基于历史搜索）
     */
    @Query("SELECT DISTINCT sh.searchKeyword FROM SearchHistory sh " +
           "WHERE sh.searchKeyword LIKE %:prefix% " +
           "ORDER BY sh.searchCount DESC, sh.updatedAt DESC")
    List<String> findSearchSuggestions(@Param("prefix") String prefix, Pageable pageable);

    /**
     * 获取用户的搜索建议
     */
    @Query("SELECT DISTINCT sh.searchKeyword FROM SearchHistory sh " +
           "WHERE sh.user = :user AND sh.searchKeyword LIKE %:prefix% " +
           "ORDER BY sh.searchCount DESC, sh.updatedAt DESC")
    List<String> findUserSearchSuggestions(@Param("user") User user, @Param("prefix") String prefix, Pageable pageable);

    /**
     * 删除用户的搜索历史
     */
    void deleteByUser(User user);

    /**
     * 删除指定时间之前的搜索历史
     */
    void deleteByCreatedAtBefore(LocalDateTime date);

    /**
     * 统计用户搜索次数
     */
    @Query("SELECT SUM(sh.searchCount) FROM SearchHistory sh WHERE sh.user = :user")
    Long countUserSearches(@Param("user") User user);

    /**
     * 获取搜索趋势数据
     */
    @Query("SELECT DATE(sh.createdAt) as searchDate, COUNT(sh) as searchCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.createdAt >= :startDate " +
           "GROUP BY DATE(sh.createdAt) " +
           "ORDER BY searchDate DESC")
    List<Object[]> getSearchTrends(@Param("startDate") LocalDateTime startDate);
}
