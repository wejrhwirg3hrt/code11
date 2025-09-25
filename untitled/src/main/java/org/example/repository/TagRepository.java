package org.example.repository;

import org.example.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签数据访问层
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 根据名称查找标签
     */
    Optional<Tag> findByName(String name);

    /**
     * 根据名称查找标签（忽略大小写）
     */
    Optional<Tag> findByNameIgnoreCase(String name);

    /**
     * 查找所有启用的标签
     */
    List<Tag> findByIsActiveTrueOrderByVideoCountDescNameAsc();

    /**
     * 根据名称模糊搜索标签
     */
    @Query("SELECT t FROM Tag t WHERE t.isActive = true AND t.name LIKE %:keyword% ORDER BY t.videoCount DESC, t.name ASC")
    List<Tag> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("keyword") String keyword);

    /**
     * 获取热门标签（按视频数量排序）
     */
    @Query("SELECT t FROM Tag t WHERE t.isActive = true AND t.videoCount > 0 ORDER BY t.videoCount DESC")
    List<Tag> findPopularTags();

    /**
     * 获取指定数量的热门标签
     */
    @Query("SELECT t FROM Tag t WHERE t.isActive = true AND t.videoCount > 0 ORDER BY t.videoCount DESC")
    List<Tag> findTopPopularTags(@Param("limit") int limit);

    /**
     * 检查标签名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(t) > 0 FROM Tag t WHERE t.name = :name AND (:id IS NULL OR t.id != :id)")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * 更新标签的视频数量
     */
    @Query("UPDATE Tag t SET t.videoCount = (SELECT COUNT(v) FROM Video v JOIN v.tags vt WHERE vt.id = t.id) WHERE t.id = :tagId")
    void updateVideoCount(@Param("tagId") Long tagId);

    /**
     * 批量更新所有标签的视频数量
     */
    @Query("UPDATE Tag t SET t.videoCount = (SELECT COUNT(v) FROM Video v JOIN v.tags vt WHERE vt.id = t.id)")
    void updateAllVideoCount();
}
