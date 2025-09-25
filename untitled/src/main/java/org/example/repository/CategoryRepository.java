package org.example.repository;

import org.example.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根据名称查找分类
     */
    Optional<Category> findByName(String name);

    /**
     * 查找所有激活的分类，按排序顺序
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * 查找所有激活的分类，按排序顺序（兼容方法）
     */
    default List<Category> findByIsActiveTrueOrderBySortOrder() {
        return findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 获取分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.isActive = true")
    long countActiveCategories();
}