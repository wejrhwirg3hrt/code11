package org.example.service;

import org.example.entity.Category;
import org.example.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 获取所有激活的分类
     */
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * 根据ID获取分类
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * 根据名称获取分类
     */
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * 创建分类
     */
    public Category createCategory(String name, String description, String icon, String color) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类名称已存在");
        }

        Category category = new Category(name, description, icon, color);
        category.setSortOrder(getNextSortOrder());
        return categoryRepository.save(category);
    }

    /**
     * 更新分类
     */
    public Category updateCategory(Long id, String name, String description, String icon, String color) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类名称已存在");
        }

        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setColor(color);

        return categoryRepository.save(category);
    }

    /**
     * 删除分类
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        categoryRepository.delete(category);
    }

    /**
     * 激活/停用分类
     */
    public void toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
    }

    /**
     * 获取下一个排序号
     */
    private Integer getNextSortOrder() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .mapToInt(c -> c.getSortOrder() != null ? c.getSortOrder() : 0)
                .max()
                .orElse(0) + 1;
    }

    /**
     * 初始化默认分类
     */
    public void initializeDefaultCategories() {
        if (categoryRepository.count() == 0) {
            createCategory("科技", "科技相关视频", "fas fa-laptop-code", "#007bff");
            createCategory("娱乐", "娱乐搞笑视频", "fas fa-smile", "#28a745");
            createCategory("教育", "教育学习视频", "fas fa-graduation-cap", "#ffc107");
            createCategory("音乐", "音乐MV视频", "fas fa-music", "#e83e8c");
            createCategory("体育", "体育运动视频", "fas fa-running", "#fd7e14");
            createCategory("游戏", "游戏相关视频", "fas fa-gamepad", "#6f42c1");
            createCategory("生活", "生活日常视频", "fas fa-home", "#20c997");
            createCategory("美食", "美食制作视频", "fas fa-utensils", "#dc3545");
        }
    }

    /**
     * 获取分类统计
     */
    public long getActiveCategoriesCount() {
        return categoryRepository.countActiveCategories();
    }
}
