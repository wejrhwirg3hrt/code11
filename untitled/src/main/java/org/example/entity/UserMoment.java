package org.example.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_moment")
public class UserMoment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String images; // JSON格式存储图片路径数组

    @Column
    private String location;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "comment_count")
    private Long commentCount = 0L;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column
    private String mood; // 心情状态

    @Column(name = "view_count")
    private Long viewCount = 0L; // 浏览次数

    // 构造函数
    public UserMoment() {
        this.createTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUserId(Long userId) {
        // 这个方法现在通过设置user对象来设置userId
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 获取图片列表（解析JSON数组）
     */
    public List<String> getImageList() {
        if (images == null || images.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 如果是JSON数组格式
            if (images.startsWith("[") && images.endsWith("]")) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(images, new TypeReference<List<String>>() {});
            } else {
                // 如果是逗号分隔格式，作为兼容处理
                List<String> result = new ArrayList<>();
                String[] paths = images.split(",");
                for (String path : paths) {
                    if (path != null && !path.trim().isEmpty()) {
                        result.add(path.trim());
                    }
                }
                return result;
            }
        } catch (Exception e) {
            // 解析失败时返回空列表
            return new ArrayList<>();
        }
    }
}
