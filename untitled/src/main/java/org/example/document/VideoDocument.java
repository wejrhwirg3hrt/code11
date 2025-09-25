package org.example.document;

import java.time.LocalDateTime;

/**
 * 视频搜索文档
 * 简化版本，用于数据库搜索
 */
public class VideoDocument {

    private String id;
    private Long videoId;
    private String title;
    private String description;
    private String[] tags;
    private String username;
    private Long views;
    private Integer likeCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
    private String status;
    private String thumbnailPath;
    private String url;

    // 构造函数
    public VideoDocument() {}

    public VideoDocument(Long videoId, String title, String description, String[] tags, 
                        String username, Long views, Integer likeCount, Integer favoriteCount,
                        LocalDateTime createdAt, String status, String thumbnailPath, String url) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.username = username;
        this.views = views;
        this.likeCount = likeCount;
        this.favoriteCount = favoriteCount;
        this.createdAt = createdAt;
        this.status = status;
        this.thumbnailPath = thumbnailPath;
        this.url = url;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
