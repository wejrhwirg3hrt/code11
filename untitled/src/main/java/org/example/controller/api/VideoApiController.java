package org.example.controller.api;

import org.example.entity.Video;
import org.example.entity.User;
import org.example.service.VideoService;
import org.example.service.UserService;
import org.example.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(originPatterns = "*")
public class VideoApiController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(videoService.getApprovedVideos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideo(@PathVariable Long id, Authentication authentication) {
        Optional<Video> video = videoService.getVideoById(id);
        if (video.isPresent()) {
            Video videoEntity = video.get();
            // 只返回已审核通过的视频
            if (videoEntity.getStatus() == Video.VideoStatus.APPROVED) {
                // 检查是否是视频所有者
                boolean isOwner = false;
                if (authentication != null) {
                    Optional<User> currentUser = userService.findByUsername(authentication.getName());
                    if (currentUser.isPresent() && videoEntity.getUser() != null &&
                            videoEntity.getUser().getId().equals(currentUser.get().getId())) {
                        isOwner = true;
                    }
                }

                // 只有非所有者访问时才增加观看量
                if (!isOwner) {
                    videoService.incrementViews(id);
                }

                return ResponseEntity.ok(videoEntity);
            }
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/search")
    public ResponseEntity<List<Video>> searchVideos(@RequestParam String q) {
        return ResponseEntity.ok(videoService.searchVideos(q));
    }

    /**
     * 现代化搜索API
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<Map<String, Object>> advancedSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false) Long minViews,
            @RequestParam(required = false) Integer minLikes,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Video> videoPage;

            if (tags != null && !tags.trim().isEmpty()) {
                // 标签搜索
                videoPage = searchService.searchByTagVideos(tags.trim(), pageable);
            } else if (sort != null) {
                // 按排序类型搜索
                videoPage = searchService.smartSearchVideos(q, sort, pageable);
            } else {
                // 高级搜索
                LocalDateTime startDate = parseTimeFilter(timeFilter, true);
                LocalDateTime endDate = parseTimeFilter(timeFilter, false);
                videoPage = searchService.advancedSearch(q, categoryId, minViews, minLikes,
                                                       startDate, endDate, pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("videos", videoPage.getContent());
            response.put("totalElements", videoPage.getTotalElements());
            response.put("totalPages", videoPage.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            response.put("hasNext", videoPage.hasNext());
            response.put("hasPrevious", videoPage.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "搜索失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 热门标签API
     */
    @GetMapping("/tags/popular")
    public ResponseEntity<List<String>> getPopularTags() {
        try {
            // 这里可以实现热门标签逻辑，暂时返回示例标签
            List<String> popularTags = Arrays.asList(
                "教程", "编程", "Java", "Spring", "前端", "后端",
                "数据库", "算法", "设计", "音乐", "游戏", "生活"
            );
            return ResponseEntity.ok(popularTags);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Arrays.asList());
        }
    }

    /**
     * 解析时间筛选
     */
    private LocalDateTime parseTimeFilter(String timeFilter, boolean isStartDate) {
        if (timeFilter == null || timeFilter.trim().isEmpty()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        switch (timeFilter.toLowerCase()) {
            case "today":
                return isStartDate ? now.toLocalDate().atStartOfDay() : now.toLocalDate().atTime(23, 59, 59);
            case "week":
                return isStartDate ? now.minusWeeks(1) : now;
            case "month":
                return isStartDate ? now.minusMonths(1) : now;
            case "year":
                return isStartDate ? now.minusYears(1) : now;
            case "4weeks":
                return isStartDate ? now.minusWeeks(4) : now;
            default:
                return null;
        }
    }

    @PostMapping
    public ResponseEntity<Video> uploadVideo(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String thumbnail,
            @RequestParam(required = false) MultipartFile videoFile,
            @RequestParam(required = false) MultipartFile thumbnailFile,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> user = userService.findByUsername(authentication.getName());
        if (!user.isPresent()) {
            return ResponseEntity.status(401).build();
        }

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setUrl(url);
        video.setThumbnail(thumbnail);
        video.setUser(user.get());
        video.setStatus(Video.VideoStatus.PENDING);

        Video savedVideo = videoService.save(video);
        return ResponseEntity.ok(savedVideo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Video> video = videoService.getVideoById(id);
        if (!video.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Optional<User> user = userService.findByUsername(authentication.getName());
        if (!user.isPresent()) {
            return ResponseEntity.status(401).build();
        }

        // 检查权限：视频所有者或管理员
        if (!video.get().getUser().getId().equals(user.get().getId()) &&
                !"ADMIN".equals(user.get().getRole())) {
            return ResponseEntity.status(403).build();
        }

        videoService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Video>> getUserVideos(@PathVariable Long userId) {
        Optional<User> user = userService.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(videoService.getVideosByUser(user.get()));
    }
}