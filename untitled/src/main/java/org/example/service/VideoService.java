package org.example.service;

import org.example.entity.Video;
import org.example.entity.User;
import org.example.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = false)
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private VideoContentService videoContentService;

    @Autowired
    private VideoLikeService videoLikeService;

    @Autowired
    private VideoFavoriteService videoFavoriteService;

    @Autowired
    private CommentService commentService;

    public long getTotalVideos() {
        return videoRepository.count();
    }

    // 获取热门视频
    // Replace the getTopVideos method:
    public List<Video> getTopVideos(int limit) {
        return videoRepository.findTop10ByOrderByViewsDescWithUser()
                .stream()
                .filter(video -> video.getStatus() == Video.VideoStatus.APPROVED)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Video> getVideosByStatus(Video.VideoStatus status) {
        return videoRepository.findByStatusWithUser(status);
    }


    public void incrementViews(Long videoId) {
        Video video = getVideoByIdInternal(videoId);
        // 使用 viewCount 字段，这是数据库中的实际字段
        Long currentViewCount = video.getViewCount() != null ? video.getViewCount() : 0L;
        video.setViewCount(currentViewCount + 1);

        // 同时更新 views 字段以保持兼容性
        Long currentViews = video.getViews() != null ? video.getViews() : 0L;
        video.setViews(currentViews + 1);

        videoRepository.save(video);
        System.out.println("✅ 观看次数已更新 - 视频ID: " + videoId + ", 新观看次数: " + video.getViewCount());
    }

    public void setFavoriteCount(Long videoId, Integer count) {
        Video video = getVideoByIdInternal(videoId);
        video.setFavoriteCount(count);
        videoRepository.save(video);
    }

    public List<Video> findAllByOrderByCreatedAtDesc() {
        PageRequest pageRequest = PageRequest.of(0, 100, Sort.by("createdAt").descending());
        return videoRepository.findAllWithUser(pageRequest).getContent();
    }

    public List<Video> findByTitleContainingIgnoreCase(String keyword) {
        PageRequest pageRequest = PageRequest.of(0, 100);
        return videoRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, pageRequest).getContent();
    }

    public List<Video> findByUserOrderByCreatedAtDesc(Long userId) {
        return videoRepository.findByUserIdWithUser(userId);
    }

    public Page<Video> findByStatus(Video.VideoStatus status, Pageable pageable) {
        return videoRepository.findByStatus(status, pageable);
    }

    public long countByStatus(Video.VideoStatus status) {
        return videoRepository.countByStatus(status);
    }

    public long countByUserId(Long userId) {
        return videoRepository.countByUserId(userId);
    }

    @Transactional(readOnly = false)
    public Video save(Video video) {
        video.setUpdatedAt(LocalDateTime.now());
        Video savedVideo = videoRepository.save(video);

        // 触发成就检查
        if (video.getUser() != null) {
            achievementService.triggerAchievementCheck(video.getUser(), "UPLOAD_VIDEO", 1);
        }

        return savedVideo;
    }

    public void deleteById(Long id) {
        videoRepository.deleteById(id);
    }
    // 获取所有视频
    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    // 审核通过视频
    public void approveVideo(Long videoId) {
        Video video = getVideoByIdInternal(videoId);
        video.setStatus(Video.VideoStatus.APPROVED);
        video.setUpdatedAt(LocalDateTime.now());
        videoRepository.save(video);
    }

    // 拒绝视频（带原因）
    public void rejectVideo(Long videoId, String reason) {
        Video video = getVideoByIdInternal(videoId);
        video.setStatus(Video.VideoStatus.REJECTED);
        video.setRejectReason(reason);
        video.setUpdatedAt(LocalDateTime.now());
        videoRepository.save(video);
    }

    // 封禁视频（带原因）
    public void banVideo(Long videoId, String reason) {
        Video video = getVideoByIdInternal(videoId);
        video.setStatus(Video.VideoStatus.BANNED);
        video.setBanReason(reason);
        video.setUpdatedAt(LocalDateTime.now());
        videoRepository.save(video);
    }

    // 修改getVideoById方法返回Optional
    public Optional<Video> getVideoById(Long id) {
        return videoRepository.findByIdWithUser(id);
    }

    // 添加一个获取Video对象的方法（用于内部调用）
    private Video getVideoByIdInternal(Long id) {
        Optional<Video> video = videoRepository.findByIdWithUser(id);
        if (video.isPresent()) {
            return video.get();
        }
        throw new RuntimeException("视频不存在");
    }

    // 添加缺失的方法
    @Transactional(readOnly = false)
    public Video saveVideo(Video video) {
        video.setUpdatedAt(LocalDateTime.now());
        Video savedVideo = videoRepository.save(video);

        // 触发成就检查
        if (video.getUser() != null) {
            achievementService.triggerAchievementCheck(video.getUser(), "UPLOAD_VIDEO", 1);
        }

        return savedVideo;
    }

    @Transactional(readOnly = false)
    public void deleteVideo(Long id) {
        // 先删除相关的视频内容
        videoContentService.deleteByVideoId(id);
        
        // 删除视频相关的点赞
        videoLikeService.deleteByVideoId(id);
        
        // 删除视频相关的收藏
        videoFavoriteService.deleteByVideoId(id);
        
        // 删除视频相关的评论
        commentService.deleteByVideoId(id);
        
        // 最后删除视频本身
        videoRepository.deleteById(id);
    }

    public List<Video> searchVideos(String keyword) {
        return videoRepository.searchByTitleOrDescription(keyword)
                .stream()
                .filter(video -> video.getStatus() == Video.VideoStatus.APPROVED)
                .collect(Collectors.toList());
    }

    public List<Video> getPopularVideos() {
        return getTopVideos(10);
    }

    public List<Video> getLatestVideos(int limit) {
        return videoRepository.findByStatusOrderByCreatedAtDescWithUser()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Video> getApprovedVideos() {
        return videoRepository.findByStatusWithUser(Video.VideoStatus.APPROVED);
    }

    public List<Video> getVideosByUser(User user) {
        return videoRepository.findByUserIdWithUser(user.getId());
    }

    /**
     * 查找用户的已审核视频（公开显示）
     */
    public List<Video> findApprovedVideosByUser(User user) {
        return videoRepository.findByUserIdAndStatusWithUser(user.getId(), Video.VideoStatus.APPROVED);
    }

    public long getPendingVideosCount() {
        return videoRepository.countByStatus(Video.VideoStatus.PENDING);
    }

    public Page<Video> getAllVideosPaged(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return videoRepository.findAllWithUser(pageRequest);
    }

    public List<Video> getPendingVideos() {
        return videoRepository.findByStatusWithUser(Video.VideoStatus.PENDING);
    }

    public Page<Video> getApprovedVideosPage(Pageable pageable) {
        // 直接返回所有已审核的视频，不过滤示例视频
        return videoRepository.findByStatusWithUser(Video.VideoStatus.APPROVED, pageable);
    }

    /**
     * 根据分类获取已审核的视频（分页）
     */
    public Page<Video> getApprovedVideosByCategory(String categoryName, Pageable pageable) {
        return videoRepository.findByStatusAndCategoryNameWithUser(categoryName, pageable);
    }

    /**
     * 临时修复方法：自动批准所有PENDING状态的视频
     */
    public void autoApproveAllPendingVideos() {
        try {
            List<Video> pendingVideos = videoRepository.findByStatus(Video.VideoStatus.PENDING);
            if (!pendingVideos.isEmpty()) {
                System.out.println("自动批准 " + pendingVideos.size() + " 个待审核视频");
                for (Video video : pendingVideos) {
                    video.setStatus(Video.VideoStatus.APPROVED);
                    videoRepository.save(video);
                }
                System.out.println("已自动批准所有待审核视频");
            }
        } catch (Exception e) {
            System.err.println("自动批准视频失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类获取视频
     */
    public List<Video> getVideosByCategory(String categoryName, int limit) {
        List<Video> videos = videoRepository.findByCategoryNameWithUser(categoryName);
        return videos.stream()
                .filter(video -> video.getStatus() == Video.VideoStatus.APPROVED)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 根据标签获取视频
     */
    public List<Video> getVideosByTag(String tagName, int limit) {
        return videoRepository.findByStatus(Video.VideoStatus.APPROVED)
                .stream()
                .filter(video -> video.getTags() != null &&
                        video.getTags().stream().anyMatch(tag -> tag.getName().equals(tagName)))
                .limit(limit)
                .collect(Collectors.toList());
    }
}