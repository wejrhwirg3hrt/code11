package org.example.dto;

/**
 * 用户统计信息DTO
 */
public class UserStats {
    private long totalVideos;
    private long totalViews;
    private long totalComments;
    private long totalLikes;
    private long totalFavorites;

    public UserStats() {}

    public UserStats(long totalVideos, long totalViews, long totalComments, long totalLikes, long totalFavorites) {
        this.totalVideos = totalVideos;
        this.totalViews = totalViews;
        this.totalComments = totalComments;
        this.totalLikes = totalLikes;
        this.totalFavorites = totalFavorites;
    }

    // Getters and Setters
    public long getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(long totalVideos) {
        this.totalVideos = totalVideos;
    }

    public long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(long totalViews) {
        this.totalViews = totalViews;
    }

    public long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(long totalComments) {
        this.totalComments = totalComments;
    }

    public long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public long getTotalFavorites() {
        return totalFavorites;
    }

    public void setTotalFavorites(long totalFavorites) {
        this.totalFavorites = totalFavorites;
    }
}
