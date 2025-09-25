package org.example.controller;

import org.example.entity.Video;
import org.example.entity.User;
import org.example.service.VideoService;
import org.example.service.AnnouncementService;
import org.example.service.CategoryService;
import org.example.service.UserService;
import org.example.entity.User;
import org.example.service.UserService;
import org.example.service.RecommendationService;
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

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       @RequestParam(defaultValue = "latest") String sort,
                       @RequestParam(required = false) String category,
                       Authentication authentication) {

        try {
            // 临时修复：自动批准所有PENDING状态的视频 - 暂时禁用
            // videoService.autoApproveAllPendingVideos();

            // 创建排序对象
            Sort sortObj = createSort(sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);

            // 根据分类筛选
            Page<Video> videoPage;
            if (category != null && !category.isEmpty()) {
                videoPage = videoService.getApprovedVideosByCategory(category, pageable);
                System.out.println("按分类筛选视频: " + category + ", 找到 " + videoPage.getTotalElements() + " 个视频");
            } else {
                videoPage = videoService.getApprovedVideosPage(pageable);
                System.out.println("获取所有已审核视频: 找到 " + videoPage.getTotalElements() + " 个视频");
            }

            // 调试信息
            System.out.println("当前页: " + page + ", 每页大小: " + size + ", 总页数: " + videoPage.getTotalPages());
            System.out.println("视频列表大小: " + videoPage.getContent().size());

            // 计算分页信息
            int totalPages = Math.max(1, videoPage.getTotalPages());
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

            model.addAttribute("videos", videoPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("totalElements", videoPage.getTotalElements());
            model.addAttribute("totalResults", videoPage.getTotalElements());
            model.addAttribute("hasNext", videoPage.hasNext());
            model.addAttribute("hasPrevious", videoPage.hasPrevious());
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentCategory", category);

            // 添加公告信息
            model.addAttribute("announcements", announcementService.getActiveAnnouncements());

            // 添加分类信息
            model.addAttribute("categories", categoryService.getAllActiveCategories());

            // 添加个性化推荐
            User currentUser = null;
            if (authentication != null) {
                Optional<User> userOpt = userService.findByUsername(authentication.getName());
                currentUser = userOpt.orElse(null);
            }

            // 添加用户信息到模型（用于前端JavaScript）
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", currentUser != null);

            // 获取推荐视频（显示在首页顶部）
            List<Video> recommendedVideos = recommendationService.getPersonalizedRecommendations(currentUser, 6);
            model.addAttribute("recommendedVideos", recommendedVideos);
            model.addAttribute("hasRecommendations", !recommendedVideos.isEmpty());
            model.addAttribute("isPersonalized", currentUser != null);

            // 初始化默认分类（如果没有分类的话）
            categoryService.initializeDefaultCategories();

            // 如果没有视频，添加提示信息
            if (videoPage.getTotalElements() == 0) {
                model.addAttribute("noVideosMessage", "暂无视频内容，快来上传第一个视频吧！");
            }

        } catch (Exception e) {
            System.err.println("首页加载异常: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "页面加载失败，请稍后重试");
        }

        return "index";
    }

    /**
     * 创建排序对象
     */
    private Sort createSort(String sortType) {
        switch (sortType) {
            case "popular":
                return Sort.by(Sort.Direction.DESC, "views", "likeCount");
            case "views":
                return Sort.by(Sort.Direction.DESC, "views");
            case "likes":
                return Sort.by(Sort.Direction.DESC, "likeCount");
            case "latest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    @GetMapping("/test-fixes")
    public String testFixes() {
        return "test-fixes";
    }

    @GetMapping("/test-features")
    public String testFeatures() {
        return "test-features";
    }

    @GetMapping("/help")
    public String helpCenter() {
        return "help-center";
    }








    @GetMapping("/test-websocket")
    public String testWebSocket(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
            }
        }
        return "test-websocket";
    }
}