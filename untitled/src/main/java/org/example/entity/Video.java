package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 添加缺失的字段以匹配数据库schema和代码期望
    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "thumbnail")
    private String thumbnail;

    // 多对多关系：视频和标签
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "video_tags",
        joinColumns = @JoinColumn(name = "video_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "images", columnDefinition = "TEXT")
    private String images; // 存储多个图片URL，用逗号分隔

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content; // 存储富文本内容（JSON格式）

    // 分类关联
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 用户关联 - 使用JoinColumn映射到user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private VideoStatus status = VideoStatus.PENDING;

    private Long views = 0L;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();


    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "duration")
    private String duration; // 视频时长，格式如 "05:30"

    @Column(name = "tags", length = 500)
    private String tagsString; // 标签字符串，用逗号分隔

    public enum VideoStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
        BANNED("已封禁");

        private final String description;

        VideoStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造函数
    public Video() {}

    // 添加缺失的字段
    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "ban_reason")
    private String banReason;

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    // 对应的getter和setter方法
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }



    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public VideoStatus getStatus() { return status; }
    public void setStatus(VideoStatus status) { this.status = status; }

    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 新增字段的getter和setter方法
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    // 标签辅助方法
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getVideos().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getVideos().remove(this);
    }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getTagsString() { return tagsString; }
    public void setTagsString(String tagsString) { this.tagsString = tagsString; }

    // 为模板兼容性添加thumbnailUrl getter
    public String getThumbnailUrl() {
        if (thumbnail != null && !thumbnail.isEmpty()) {
            return thumbnail;
        }
        if (thumbnailPath != null && !thumbnailPath.isEmpty()) {
            return thumbnailPath;
        }
        return "/images/default-thumbnail.jpg";
    }
}