package org.example.service;

import org.example.entity.Tag;
import org.example.entity.User;
import org.example.entity.Video;
import org.example.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签服务
 */
@Service
@Transactional(readOnly = false)
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    /**
     * 获取所有启用的标签
     */
    public List<Tag> getAllActiveTags() {
        return tagRepository.findByIsActiveTrueOrderByVideoCountDescNameAsc();
    }

    /**
     * 获取热门标签
     */
    public List<Tag> getPopularTags(int limit) {
        List<Tag> popularTags = tagRepository.findPopularTags();
        return popularTags.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查找标签
     */
    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }

    /**
     * 根据名称查找标签
     */
    public Optional<Tag> findByName(String name) {
        return tagRepository.findByNameIgnoreCase(name.trim());
    }

    /**
     * 搜索标签
     */
    public List<Tag> searchTags(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveTags();
        }
        return tagRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword.trim());
    }

    /**
     * 创建新标签
     */
    public Tag createTag(String name, String description, String color, User creator) {
        // 检查标签名称是否已存在
        if (tagRepository.findByNameIgnoreCase(name.trim()).isPresent()) {
            throw new IllegalArgumentException("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setName(name.trim());
        tag.setDescription(description != null ? description.trim() : "");
        tag.setColor(color != null && !color.trim().isEmpty() ? color.trim() : "#007bff");
        tag.setCreatedBy(creator);
        tag.setCreatedAt(LocalDateTime.now());
        tag.setIsActive(true);
        tag.setVideoCount(0L);

        return tagRepository.save(tag);
    }

    /**
     * 更新标签
     */
    public Tag updateTag(Long id, String name, String description, String color) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("标签不存在"));

        // 检查名称是否与其他标签冲突
        if (tagRepository.existsByNameAndIdNot(name.trim(), id)) {
            throw new IllegalArgumentException("标签名称已存在");
        }

        tag.setName(name.trim());
        tag.setDescription(description != null ? description.trim() : "");
        tag.setColor(color != null && !color.trim().isEmpty() ? color.trim() : "#007bff");
        tag.setUpdatedAt(LocalDateTime.now());

        return tagRepository.save(tag);
    }

    /**
     * 删除标签
     */
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("标签不存在"));
        
        // 从所有视频中移除该标签
        for (Video video : tag.getVideos()) {
            video.getTags().remove(tag);
        }
        
        tagRepository.delete(tag);
    }

    /**
     * 启用/禁用标签
     */
    public Tag toggleTagStatus(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("标签不存在"));
        
        tag.setIsActive(!tag.getIsActive());
        tag.setUpdatedAt(LocalDateTime.now());
        
        return tagRepository.save(tag);
    }

    /**
     * 根据名称列表获取或创建标签
     */
    public Set<Tag> getOrCreateTags(List<String> tagNames, User creator) {
        Set<Tag> tags = new HashSet<>();
        
        if (tagNames == null || tagNames.isEmpty()) {
            return tags;
        }
        
        for (String tagName : tagNames) {
            if (tagName != null && !tagName.trim().isEmpty()) {
                String cleanName = tagName.trim();
                Optional<Tag> existingTag = findByName(cleanName);
                
                if (existingTag.isPresent()) {
                    tags.add(existingTag.get());
                } else {
                    // 创建新标签
                    try {
                        Tag newTag = createTag(cleanName, "", generateRandomColor(), creator);
                        tags.add(newTag);
                    } catch (Exception e) {
                        System.err.println("创建标签失败: " + cleanName + ", 错误: " + e.getMessage());
                    }
                }
            }
        }
        
        return tags;
    }

    /**
     * 更新标签的视频数量
     */
    public void updateTagVideoCount(Long tagId) {
        tagRepository.updateVideoCount(tagId);
    }

    /**
     * 更新所有标签的视频数量
     */
    public void updateAllTagVideoCount() {
        tagRepository.updateAllVideoCount();
    }

    /**
     * 生成随机颜色
     */
    private String generateRandomColor() {
        String[] colors = {
            "#007bff", "#28a745", "#dc3545", "#ffc107", "#17a2b8",
            "#6f42c1", "#e83e8c", "#fd7e14", "#20c997", "#6c757d"
        };
        Random random = new Random();
        return colors[random.nextInt(colors.length)];
    }

    /**
     * 获取标签统计信息
     */
    public Map<String, Object> getTagStatistics() {
        List<Tag> allTags = tagRepository.findAll();
        long totalTags = allTags.size();
        long activeTags = allTags.stream().filter(Tag::getIsActive).count();
        long totalVideoCount = allTags.stream().mapToLong(Tag::getVideoCount).sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTags", totalTags);
        stats.put("activeTags", activeTags);
        stats.put("totalVideoCount", totalVideoCount);
        stats.put("averageVideosPerTag", activeTags > 0 ? (double) totalVideoCount / activeTags : 0.0);
        
        return stats;
    }
}
