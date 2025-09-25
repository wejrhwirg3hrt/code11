package org.example.service;

import org.example.document.VideoDocument;
import org.example.entity.SearchHistory;
import org.example.entity.Tag;
import org.example.entity.User;
import org.example.entity.Video;
import org.example.repository.SearchHistoryRepository;
import org.example.repository.VideoRepository;
// import org.example.repository.elasticsearch.VideoSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 搜索服务
 * 提供基于MySQL数据库的搜索功能
 */
@Service
@Transactional(readOnly = false)
public class SearchService {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;



    /**
     * 智能搜索 - 多字段匹配
     */
    public Page<VideoDocument> smartSearch(String keyword, Pageable pageable) {
        return searchWithDatabase(keyword, pageable);
    }

    /**
     * 高级搜索 - 支持多条件
     */
    public Page<VideoDocument> advancedSearch(String keyword, String status, Long minViews, Pageable pageable) {
        return searchWithDatabase(keyword, pageable);
    }

    /**
     * 按用户搜索
     */
    public Page<VideoDocument> searchByUser(String username, Pageable pageable) {
        return searchWithDatabase(username, pageable);
    }

    /**
     * 获取最新视频
     */
    public Page<VideoDocument> getLatestVideos(Pageable pageable) {
        List<Video> videos = videoService.getAllVideos().stream()
                .filter(v -> v.getStatus() == Video.VideoStatus.APPROVED)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
        return convertToDocumentPage(videos, pageable);
    }



    /**
     * 搜索建议 - 自动补全
     */
    public List<String> getSearchSuggestions(String prefix) {
        return videoService.getAllVideos().stream()
                .filter(v -> v.getTitle().toLowerCase().contains(prefix.toLowerCase()))
                .map(Video::getTitle)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 数据库搜索降级方法
     */
    private Page<VideoDocument> searchWithDatabase(String keyword, Pageable pageable) {
        List<Video> allVideos = videoService.getAllVideos();
        List<Video> filteredVideos;

        if (keyword == null || keyword.trim().isEmpty()) {
            filteredVideos = allVideos.stream()
                    .filter(v -> v.getStatus() == Video.VideoStatus.APPROVED)
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(Collectors.toList());
        } else {
            String lowerKeyword = keyword.toLowerCase();
            filteredVideos = allVideos.stream()
                    .filter(v -> v.getStatus() == Video.VideoStatus.APPROVED)
                    .filter(v -> v.getTitle().toLowerCase().contains(lowerKeyword) ||
                               (v.getDescription() != null && v.getDescription().toLowerCase().contains(lowerKeyword)) ||
                               (v.getTags() != null && v.getTags().stream().anyMatch(tag -> tag.getName().toLowerCase().contains(lowerKeyword))))
                    .collect(Collectors.toList());
        }

        return convertToDocumentPage(filteredVideos, pageable);
    }

    /**
     * 将Video列表转换为VideoDocument分页对象
     */
    private Page<VideoDocument> convertToDocumentPage(List<Video> videos, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), videos.size());

        List<VideoDocument> documents = videos.subList(start, end).stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());

        return new PageImpl<>(documents, pageable, videos.size());
    }

    /**
     * 将Video分页对象转换为VideoDocument分页对象
     */
    private Page<VideoDocument> convertToDocumentPage(Page<Video> videoPage) {
        List<VideoDocument> documents = videoPage.getContent().stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());

        return new PageImpl<>(documents, videoPage.getPageable(), videoPage.getTotalElements());
    }

    /**
     * 将Video实体转换为VideoDocument
     */
    private VideoDocument convertToDocument(Video video) {
        String[] tags = video.getTags() != null && !video.getTags().isEmpty() ?
                video.getTags().stream().map(Tag::getName).toArray(String[]::new) : new String[0];
        
        return new VideoDocument(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                tags,
                video.getUser() != null ? video.getUser().getUsername() : "unknown",
                video.getViews(),
                video.getLikeCount(),
                video.getFavoriteCount(),
                video.getCreatedAt(),
                video.getStatus().toString(),
                video.getThumbnailPath(),
                video.getUrl()
        );
    }



    /**
     * 记录搜索历史
     */
    @Transactional
    public void recordSearchHistory(User user, String keyword, int resultCount, HttpServletRequest request) {
        if (user == null || keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        String cleanKeyword = keyword.trim();

        // 查找是否已存在相同的搜索记录
        Optional<SearchHistory> existingHistory = searchHistoryRepository.findByUserAndSearchKeyword(user, cleanKeyword);

        if (existingHistory.isPresent()) {
            // 更新现有记录
            SearchHistory history = existingHistory.get();
            history.setSearchCount(history.getSearchCount() + 1);
            history.setResultCount(resultCount);
            history.setUpdatedAt(LocalDateTime.now());
            searchHistoryRepository.save(history);
        } else {
            // 创建新记录
            SearchHistory history = new SearchHistory(user, cleanKeyword, resultCount);
            if (request != null) {
                history.setIpAddress(getClientIpAddress(request));
                history.setUserAgent(request.getHeader("User-Agent"));
            }
            searchHistoryRepository.save(history);
        }
    }

    /**
     * 获取用户搜索历史
     */
    public List<SearchHistory> getUserSearchHistory(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return searchHistoryRepository.findRecentSearchHistory(user, pageable);
    }

    /**
     * 获取搜索建议
     */
    public List<String> getSearchSuggestions(String prefix, User user, int limit) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, limit);

        if (user != null) {
            // 优先返回用户的搜索历史建议
            List<String> userSuggestions = searchHistoryRepository.findUserSearchSuggestions(user, prefix, pageable);
            if (!userSuggestions.isEmpty()) {
                return userSuggestions;
            }
        }

        // 返回全局搜索建议
        return searchHistoryRepository.findSearchSuggestions(prefix, pageable);
    }

    /**
     * 获取热门搜索关键词
     */
    public List<String> getHotSearchKeywords(int limit) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7); // 最近7天
        Pageable pageable = PageRequest.of(0, limit);

        return searchHistoryRepository.findHotSearchKeywords(startDate, pageable)
                .stream()
                .map(result -> (String) result[0])
                .collect(Collectors.toList());
    }

    /**
     * 清除用户搜索历史
     */
    @Transactional
    public void clearUserSearchHistory(User user) {
        searchHistoryRepository.deleteByUser(user);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // ==================== 现代化搜索引擎方法 ====================

    /**
     * 标签搜索 - 返回VideoDocument（API兼容）
     */
    public Page<VideoDocument> searchByTag(String tag, Pageable pageable) {
        Page<Video> videoPage = videoRepository.findByTagsContaining(tag, pageable);
        return convertToDocumentPage(videoPage);
    }

    /**
     * 标签搜索 - 返回Video实体（内部使用）
     */
    public Page<Video> searchByTagVideos(String tag, Pageable pageable) {
        return videoRepository.findByTagsContaining(tag, pageable);
    }

    /**
     * 多标签搜索
     */
    public Page<Video> searchByMultipleTags(List<String> tags, Pageable pageable) {
        String tag1 = tags.size() > 0 ? tags.get(0) : null;
        String tag2 = tags.size() > 1 ? tags.get(1) : null;
        String tag3 = tags.size() > 2 ? tags.get(2) : null;
        return videoRepository.findByMultipleTags(tag1, tag2, tag3, pageable);
    }

    /**
     * 最热门视频（按观看次数）- 返回VideoDocument（API兼容）
     */
    public Page<VideoDocument> getPopularVideos(Pageable pageable) {
        Page<Video> videoPage = videoRepository.findAllOrderByViewCountDesc(pageable);
        return convertToDocumentPage(videoPage);
    }

    /**
     * 最热门视频（按观看次数）- 返回Video实体（内部使用）
     */
    public Page<Video> getPopularVideosEntities(Pageable pageable) {
        return videoRepository.findAllOrderByViewCountDesc(pageable);
    }

    /**
     * 最多点赞视频
     */
    public Page<Video> getMostLikedVideos(Pageable pageable) {
        return videoRepository.findAllOrderByLikeCountDesc(pageable);
    }

    /**
     * 4周内最热门视频
     */
    public Page<Video> getPopularInLast4Weeks(Pageable pageable) {
        LocalDateTime fourWeeksAgo = LocalDateTime.now().minusWeeks(4);
        return videoRepository.findPopularInLast4Weeks(fourWeeksAgo, pageable);
    }

    /**
     * 4周内最多点赞视频
     */
    public Page<Video> getMostLikedInLast4Weeks(Pageable pageable) {
        LocalDateTime fourWeeksAgo = LocalDateTime.now().minusWeeks(4);
        return videoRepository.findMostLikedInLast4Weeks(fourWeeksAgo, pageable);
    }

    /**
     * 综合搜索（标题、描述、标签）
     */
    public Page<Video> comprehensiveSearch(String keyword, Pageable pageable) {
        return videoRepository.findByKeywordInTitleDescriptionOrTags(keyword, pageable);
    }

    /**
     * 分类搜索
     */
    public Page<Video> searchByCategory(Long categoryId, String keyword, Pageable pageable) {
        return videoRepository.findByCategoryAndKeyword(categoryId, keyword, pageable);
    }

    /**
     * 高级搜索
     */
    public Page<Video> advancedSearch(String keyword, Long categoryId, Long minViews,
                                      Integer minLikes, LocalDateTime startDate,
                                      LocalDateTime endDate, Pageable pageable) {
        return videoRepository.findByAdvancedFilters(keyword, categoryId, minViews,
                                                     minLikes, startDate, endDate, pageable);
    }

    /**
     * 智能排序搜索
     */
    public Page<Video> smartSearchVideos(String keyword, String sortBy, Pageable pageable) {
        // 根据排序类型调用不同的搜索方法
        switch (sortBy.toLowerCase()) {
            case "popular":
            case "views":
                return getPopularVideosEntities(pageable);
            case "likes":
                return getMostLikedVideos(pageable);
            case "popular_4weeks":
                return getPopularInLast4Weeks(pageable);
            case "likes_4weeks":
                return getMostLikedInLast4Weeks(pageable);
            case "latest":
            default:
                if (keyword != null && !keyword.trim().isEmpty()) {
                    return comprehensiveSearch(keyword, pageable);
                } else {
                    // 如果没有关键词，返回最新视频
                    return videoRepository.findByStatus(Video.VideoStatus.APPROVED, pageable);
                }
        }
    }
}
