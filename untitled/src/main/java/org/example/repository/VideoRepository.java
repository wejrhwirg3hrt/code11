package org.example.repository;

import org.example.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByStatus(Video.VideoStatus status);

    Page<Video> findByStatus(Video.VideoStatus status, Pageable pageable);

    long countByStatus(Video.VideoStatus status);

    List<Video> findByUserId(Long userId);

    List<Video> findByUserIdAndStatus(Long userId, Video.VideoStatus status);

    Page<Video> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.title LIKE %:keyword% OR v.description LIKE %:keyword%")
    Page<Video> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT v FROM Video v ORDER BY v.views DESC")
    List<Video> findTopByOrderByViewsDesc(Pageable pageable);

    @Query("SELECT v FROM Video v ORDER BY v.createdAt DESC")
    List<Video> findByOrderByCreatedAtDesc(Pageable pageable);

    long countByUserId(Long userId);

    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' ORDER BY v.views DESC")
    List<Video> findTop10ByOrderByViewsDesc();

    // 根据状态统计数量（字符串版本，兼容现有代码）
    @Query("SELECT COUNT(v) FROM Video v WHERE v.status = ?1")
    long countByStatus(String status);

    // 搜索视频
    @Query("SELECT v FROM Video v WHERE v.title LIKE %?1% OR v.description LIKE %?1%")
    List<Video> searchByTitleOrDescription(String keyword);

    // 查询方法（用于安全删除）
    List<Video> findByUrlContaining(String urlPattern);
    List<Video> findByThumbnailContaining(String thumbnailPattern);
    List<Video> findByTitle(String title);

    // 清理样例视频的方法（保留但不推荐在事务外使用）
    int deleteByUrlContaining(String urlPattern);
    int deleteByThumbnailContaining(String thumbnailPattern);
    int deleteByTitle(String title);

    // ==================== 现代化搜索引擎方法 ====================

    /**
     * 根据标签搜索视频
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' AND v.tagsString LIKE %:tag%")
    Page<Video> findByTagsContaining(@Param("tag") String tag, Pageable pageable);

    /**
     * 多标签搜索（AND逻辑）
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' AND " +
           "(:tag1 IS NULL OR v.tagsString LIKE %:tag1%) AND " +
           "(:tag2 IS NULL OR v.tagsString LIKE %:tag2%) AND " +
           "(:tag3 IS NULL OR v.tagsString LIKE %:tag3%)")
    Page<Video> findByMultipleTags(@Param("tag1") String tag1,
                                   @Param("tag2") String tag2,
                                   @Param("tag3") String tag3,
                                   Pageable pageable);

    /**
     * 按观看次数排序（最热门）
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' ORDER BY v.viewCount DESC")
    Page<Video> findAllOrderByViewCountDesc(Pageable pageable);

    /**
     * 按点赞数排序
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' ORDER BY v.likeCount DESC")
    Page<Video> findAllOrderByLikeCountDesc(Pageable pageable);

    /**
     * 4周内最热门视频（按观看次数）
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' AND v.createdAt >= :fourWeeksAgo ORDER BY v.viewCount DESC")
    Page<Video> findPopularInLast4Weeks(@Param("fourWeeksAgo") java.time.LocalDateTime fourWeeksAgo, Pageable pageable);

    /**
     * 4周内最多点赞视频
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' AND v.createdAt >= :fourWeeksAgo ORDER BY v.likeCount DESC")
    Page<Video> findMostLikedInLast4Weeks(@Param("fourWeeksAgo") java.time.LocalDateTime fourWeeksAgo, Pageable pageable);

    /**
     * 综合搜索：标题、描述、标签
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' AND " +
           "(v.title LIKE %:keyword% OR v.description LIKE %:keyword% OR v.tagsString LIKE %:keyword%)")
    Page<Video> findByKeywordInTitleDescriptionOrTags(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分类 + 关键词搜索
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' AND " +
           "(:categoryId IS NULL OR v.category.id = :categoryId) AND " +
           "(:keyword IS NULL OR v.title LIKE %:keyword% OR v.description LIKE %:keyword% OR v.tagsString LIKE %:keyword%)")
    Page<Video> findByCategoryAndKeyword(@Param("categoryId") Long categoryId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    /**
     * 高级筛选搜索
     */
    @Query("SELECT v FROM Video v WHERE v.status = 'APPROVED' AND " +
           "(:keyword IS NULL OR v.title LIKE %:keyword% OR v.description LIKE %:keyword% OR v.tagsString LIKE %:keyword%) AND " +
           "(:categoryId IS NULL OR v.category.id = :categoryId) AND " +
           "(:minViews IS NULL OR v.viewCount >= :minViews) AND " +
           "(:minLikes IS NULL OR v.likeCount >= :minLikes) AND " +
           "(:startDate IS NULL OR v.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR v.createdAt <= :endDate)")
    Page<Video> findByAdvancedFilters(@Param("keyword") String keyword,
                                      @Param("categoryId") Long categoryId,
                                      @Param("minViews") Long minViews,
                                      @Param("minLikes") Integer minLikes,
                                      @Param("startDate") java.time.LocalDateTime startDate,
                                      @Param("endDate") java.time.LocalDateTime endDate,
                                      Pageable pageable);

    // 按状态和创建时间排序查询
    List<Video> findByStatusOrderByCreatedAtDesc(Video.VideoStatus status);

    // 根据状态和分类查询视频（分页）
    @Query("SELECT v FROM Video v WHERE v.status = ?1 AND v.category.name = ?2")
    Page<Video> findByStatusAndCategoryName(Video.VideoStatus status, String categoryName, Pageable pageable);

    // 根据分类查询视频
    @Query("SELECT v FROM Video v WHERE v.category.name = ?1")
    List<Video> findByCategoryName(String categoryName);

    // 根据分类ID和状态统计数量
    @Query("SELECT COUNT(v) FROM Video v WHERE v.category.id = ?1 AND v.status = ?2")
    long countByCategoryIdAndStatus(Long categoryId, Video.VideoStatus status);

    // 根据分类ID和状态查询视频
    @Query("SELECT v FROM Video v WHERE v.category.id = ?1 AND v.status = ?2")
    List<Video> findByCategoryIdAndStatus(Long categoryId, Video.VideoStatus status);

    // ==================== 成就系统相关查询方法 ====================

    /**
     * 统计用户在周末上传的视频数量
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.userId = :userId AND " +
           "(DAYOFWEEK(v.createdAt) = 1 OR DAYOFWEEK(v.createdAt) = 7)")
    long countWeekendUploadsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户上传视频的不同分类数量
     */
    @Query("SELECT COUNT(DISTINCT v.category.id) FROM Video v WHERE v.userId = :userId AND v.category IS NOT NULL")
    long countDistinctCategoriesByUserId(@Param("userId") Long userId);

    /**
     * 统计用户上传的短视频数量（小于5分钟）
     * 由于duration是字符串格式（如"05:30"），暂时返回0，需要在Service层处理
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.userId = :userId")
    long countShortVideosByUserId(@Param("userId") Long userId);

    /**
     * 统计用户上传的长视频数量（大于30分钟）
     * 由于duration是字符串格式（如"05:30"），暂时返回0，需要在Service层处理
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.userId = :userId")
    long countLongVideosByUserId(@Param("userId") Long userId);

    /**
     * 统计用户上传的超长视频数量（大于60分钟）
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.userId = :userId AND CAST(v.duration AS int) > 3600")
    long countMarathonVideosByUserId(@Param("userId") Long userId);

    /**
     * 计算用户视频的平均点赞数
     */
    @Query("SELECT AVG(v.likeCount) FROM Video v WHERE v.userId = :userId")
    Double getAverageLikesByUserId(@Param("userId") Long userId);

    /**
     * 统计用户获得的总点赞数
     */
    @Query("SELECT COALESCE(SUM(v.likeCount), 0) FROM Video v WHERE v.userId = :userId")
    long getTotalLikesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态统计视频数量
     */
    long countByUserIdAndStatus(Long userId, Video.VideoStatus status);

    /**
     * 统计用户视频的总观看次数
     */
    @Query("SELECT COALESCE(SUM(v.viewCount), 0) FROM Video v WHERE v.userId = :userId")
    Long sumViewCountByUserId(@Param("userId") Long userId);

    // 添加JOIN FETCH查询来解决懒加载问题
    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user ORDER BY v.createdAt DESC")
    Page<Video> findAllWithUser(Pageable pageable);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user WHERE v.status = :status ORDER BY v.createdAt DESC")
    List<Video> findByStatusWithUser(@Param("status") Video.VideoStatus status);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user WHERE v.status = :status ORDER BY v.createdAt DESC")
    Page<Video> findByStatusWithUser(@Param("status") Video.VideoStatus status, Pageable pageable);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user WHERE v.status = 'APPROVED' ORDER BY v.views DESC")
    List<Video> findTop10ByOrderByViewsDescWithUser();

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user WHERE v.status = 'APPROVED' ORDER BY v.createdAt DESC")
    List<Video> findByStatusOrderByCreatedAtDescWithUser();

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user WHERE v.userId = :userId ORDER BY v.createdAt DESC")
    List<Video> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user WHERE v.userId = :userId AND v.status = :status ORDER BY v.createdAt DESC")
    List<Video> findByUserIdAndStatusWithUser(@Param("userId") Long userId, @Param("status") Video.VideoStatus status);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user WHERE v.status = 'APPROVED' AND v.category.name = :categoryName ORDER BY v.createdAt DESC")
    Page<Video> findByStatusAndCategoryNameWithUser(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user WHERE v.category.name = :categoryName ORDER BY v.createdAt DESC")
    List<Video> findByCategoryNameWithUser(@Param("categoryName") String categoryName);

    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.category LEFT JOIN FETCH v.tags WHERE v.id = :id")
    Optional<Video> findByIdWithUser(@Param("id") Long id);
}
