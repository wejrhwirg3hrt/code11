package org.example.controller;

import org.example.entity.Video;
import org.example.entity.User;
import org.example.service.VideoService;
import org.example.service.CategoryService;
import org.example.service.SearchService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size,
                        @RequestParam(required = false) String category,
                        @RequestParam(defaultValue = "latest") String sort,
                        @RequestParam(required = false) String timeFilter,
                        @RequestParam(required = false) Long minViews,
                        @RequestParam(required = false) Integer minLikes,
                        @RequestParam(required = false) String tags, // 新增标签搜索参数
                        Model model,
                        Authentication authentication,
                        HttpServletRequest request) {

        System.out.println("=== 搜索请求参数 ===");
        System.out.println("关键词: " + q);
        System.out.println("页码: " + page);
        System.out.println("排序: " + sort);
        System.out.println("分类: " + category);

        // 设置分页
        Pageable pageable = PageRequest.of(page, size);

        Page<Video> videoPage;
        long totalResults = 0;

        // 获取当前用户
        User currentUser = null;
        if (authentication != null) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            currentUser = userOpt.orElse(null);
        }

        try {
            // 现代化搜索逻辑
            if (tags != null && !tags.trim().isEmpty()) {
                // 标签搜索
                videoPage = searchService.searchByTagVideos(tags.trim(), pageable);
            } else if (category != null && !category.trim().isEmpty()) {
                // 分类搜索
                Long categoryId = parseCategoryId(category);
                videoPage = searchService.searchByCategory(categoryId, q, pageable);
            } else {
                // 智能搜索（根据排序类型）
                switch (sort.toLowerCase()) {
                    case "popular":
                    case "views":
                        videoPage = searchService.getPopularVideosEntities(pageable);
                        break;
                    case "likes":
                        videoPage = searchService.getMostLikedVideos(pageable);
                        break;
                    case "popular_4weeks":
                        videoPage = searchService.getPopularInLast4Weeks(pageable);
                        break;
                    case "likes_4weeks":
                        videoPage = searchService.getMostLikedInLast4Weeks(pageable);
                        break;
                    case "latest":
                    default:
                        if (q != null && !q.trim().isEmpty()) {
                            // 高级搜索
                            LocalDateTime startDate = parseTimeFilter(timeFilter, true);
                            LocalDateTime endDate = parseTimeFilter(timeFilter, false);
                            Long categoryIdFilter = parseCategoryId(category);
                            videoPage = searchService.advancedSearch(q.trim(), categoryIdFilter,
                                                                   minViews, minLikes, startDate, endDate, pageable);
                        } else {
                            // 默认显示最新视频
                            videoPage = videoService.getApprovedVideosPage(pageable);
                        }
                        break;
                }
            }

            totalResults = videoPage.getTotalElements();

            // 记录搜索历史
            if (currentUser != null && q != null && !q.trim().isEmpty()) {
                try {
                    searchService.recordSearchHistory(currentUser, q.trim(), (int) totalResults, request);
                } catch (Exception e) {
                    System.err.println("记录搜索历史失败: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("搜索出错: " + e.getMessage());
            e.printStackTrace();
            // 出错时返回空结果
            videoPage = Page.empty(pageable);
            totalResults = 0;
        }

        // 获取视频列表
        List<Video> videos = videoPage.getContent();

        // 调试信息
        System.out.println("搜索结果数量: " + videos.size());
        for (Video video : videos) {
            System.out.println("视频: " + video.getTitle() + ", 状态: " + video.getStatus());
        }

        // 添加智能推荐
        List<Video> recommendedVideos = videoService.getTopVideos(6);
        List<Video> latestVideos = videoService.getLatestVideos(6);

        // 计算分页信息
        int totalPages = Math.max(1, videoPage.getTotalPages()); // 确保至少为1
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(totalPages - 1, page + 2);

        // 如果页数不足5页，调整显示范围
        if (endPage - startPage < 4) {
            if (startPage == 0) {
                endPage = Math.min(totalPages - 1, startPage + 4);
            } else if (endPage == totalPages - 1) {
                startPage = Math.max(0, endPage - 4);
            }
        }

        // 调试信息
        System.out.println("=== 分页调试信息 ===");
        System.out.println("视频数量: " + videos.size());
        System.out.println("总结果数: " + totalResults);
        System.out.println("当前页: " + page);
        System.out.println("每页大小: " + size);
        System.out.println("总页数: " + totalPages);
        System.out.println("开始页: " + startPage);
        System.out.println("结束页: " + endPage);

        // 添加模型属性
        model.addAttribute("videos", videos);
        model.addAttribute("keyword", q);
        model.addAttribute("query", q);
        model.addAttribute("totalResults", totalResults);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("recommendedVideos", recommendedVideos);
        model.addAttribute("latestVideos", latestVideos);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentTimeFilter", timeFilter);
        model.addAttribute("minViews", minViews);
        model.addAttribute("minLikes", minLikes);

        return "search";
    }

    /**
     * 应用高级筛选条件
     */
    private List<Video> applyAdvancedFilters(List<Video> videos, String category, String timeFilter,
                                           Long minViews, Integer minLikes) {
        return videos.stream()
                .filter(video -> {
                    // 分类筛选
                    if (category != null && !category.isEmpty() && !"all".equals(category)) {
                        if (video.getCategory() == null || !category.equals(video.getCategory().getName())) {
                            return false;
                        }
                    }

                    // 时间筛选
                    if (timeFilter != null && !timeFilter.isEmpty() && !"all".equals(timeFilter)) {
                        if (!matchesTimeFilter(video, timeFilter)) {
                            return false;
                        }
                    }

                    // 观看次数筛选
                    if (minViews != null && video.getViews() < minViews) {
                        return false;
                    }

                    // 点赞数筛选
                    if (minLikes != null && video.getLikeCount() < minLikes) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查视频是否匹配时间筛选条件
     */
    private boolean matchesTimeFilter(Video video, String timeFilter) {
        if (video.getCreatedAt() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime videoTime = video.getCreatedAt();

        switch (timeFilter) {
            case "today":
                return videoTime.toLocalDate().equals(now.toLocalDate());
            case "week":
                return videoTime.isAfter(now.minusWeeks(1));
            case "month":
                return videoTime.isAfter(now.minusMonths(1));
            case "year":
                return videoTime.isAfter(now.minusYears(1));
            default:
                return true;
        }
    }
    
    /**
     * 根据排序参数获取Sort对象
     */
    private Sort getSortOrder(String sort) {
        switch (sort) {
            case "views":
                return Sort.by("views").descending();
            case "likes":
                return Sort.by("likeCount").descending();
            case "favorites":
                return Sort.by("favoriteCount").descending();
            case "latest":
            default:
                return Sort.by("createdAt").descending();
        }
    }

    /**
     * 解析分类ID
     */
    private Long parseCategoryId(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(category);
        } catch (NumberFormatException e) {
            // 如果不是数字，可能是分类名称，需要通过CategoryService查找
            return categoryService.getCategoryByName(category)
                    .map(cat -> cat.getId())
                    .orElse(null);
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
}
