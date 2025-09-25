package org.example.controller;

import org.example.dto.UserStats;
import org.example.entity.User;
import org.example.entity.UserLevel;
import org.example.entity.Video;
import org.example.entity.VideoFavorite;
import org.example.entity.VideoLike;
import org.example.repository.VideoFavoriteRepository;
import org.example.repository.VideoLikeRepository;
import org.example.service.UserService;
import org.example.service.VideoService;
import org.example.service.StatisticsService;
import org.example.service.UserLevelService;
import org.example.service.AchievementService;
import org.example.service.AchievementFixService;
import org.example.service.FastStatsService;
import org.example.service.AchievementAutoDetectionService;
import org.example.service.UltraFastStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.io.File;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoFavoriteRepository videoFavoriteRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private AchievementFixService achievementFixService;

    @Autowired
    private FastStatsService fastStatsService;

    @Autowired
    private AchievementAutoDetectionService achievementAutoDetectionService;

    @Autowired
    private UltraFastStatsService ultraFastStatsService;

    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> user = userService.findByUsername(authentication.getName());
        if (user.isPresent()) {
            User currentUser = user.get();
            List<Video> userVideos = videoService.getVideosByUser(currentUser);
            List<VideoFavorite> favorites = videoFavoriteRepository.findByUserOrderByCreatedAtDesc(currentUser);
            List<Video> favoriteVideos = favorites.stream()
                    .map(VideoFavorite::getVideo)
                    .collect(Collectors.toList());

            List<VideoLike> likes = videoLikeRepository.findByUserOrderByCreatedAtDesc(currentUser);
            List<Video> likedVideos = likes.stream()
                    .map(VideoLike::getVideo)
                    .collect(Collectors.toList());

            // 获取用户统计数据
            Map<String, Object> userStatsMap = statisticsService.getUserStatistics(currentUser);

            // 创建UserStats对象
            UserStats userStats = new UserStats(
                userVideos.size(),
                userVideos.stream().mapToLong(v -> v.getViews() != null ? v.getViews() : 0).sum(),
                0L, // TODO: 实现评论统计
                userVideos.stream().mapToLong(v -> v.getLikeCount() != null ? v.getLikeCount() : 0).sum(),
                userVideos.stream().mapToLong(v -> v.getFavoriteCount() != null ? v.getFavoriteCount() : 0).sum()
            );

            model.addAttribute("user", currentUser);
            model.addAttribute("videos", userVideos);
            model.addAttribute("favoriteVideos", favoriteVideos);
            model.addAttribute("likedVideos", likedVideos);
            model.addAttribute("userStats", userStats);
        }

        return "profile";
    }



    @GetMapping("/stats")
    public String userStats(Model model, Authentication authentication) {
        long startTime = System.currentTimeMillis();

        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            // 获取当前用户
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                return "redirect:/login";
            }

            User user = userOpt.get();

            // 使用极速服务 - 0数据库查询，但返回原来的模板
            Map<String, Object> allData = ultraFastStatsService.getAllDataUltraFast(user);

            // 添加到模型
            model.addAttribute("user", user);
            model.addAttribute("userStats", allData.get("userStats"));
            model.addAttribute("userLevel", allData.get("userLevel"));
            model.addAttribute("achievementProgress", allData.get("achievementProgress"));
            model.addAttribute("levelProgress", allData.get("levelProgress"));
            model.addAttribute("nextLevelExp", allData.get("nextLevelExp"));

            long endTime = System.currentTimeMillis();
            System.out.println("⚡ 极速加载原始统计页面完成: " + user.getUsername() +
                " (耗时: " + (endTime - startTime) + "ms)");

        } catch (Exception e) {
            System.err.println("加载用户统计页面失败: " + e.getMessage());
            e.printStackTrace();
        }

        // 返回新的简化模板
        return "user-stats-new";
    }

    /**
     * 极速统计页面 - 专门用于解决刷新慢的问题
     */
    @GetMapping("/ultra-stats")
    public String ultraStats(Model model, Authentication authentication) {
        long startTime = System.currentTimeMillis();

        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            // 获取当前用户 - 这是唯一的数据库查询
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                return "redirect:/login";
            }

            User user = userOpt.get();

            // 使用极速服务 - 完全避免数据库查询
            Map<String, Object> allData = ultraFastStatsService.getAllDataUltraFast(user);

            // 添加到模型
            model.addAttribute("user", user);
            model.addAttribute("userStats", allData.get("userStats"));
            model.addAttribute("userLevel", allData.get("userLevel"));
            model.addAttribute("achievementProgress", allData.get("achievementProgress"));
            model.addAttribute("levelProgress", allData.get("levelProgress"));
            model.addAttribute("nextLevelExp", allData.get("nextLevelExp"));

            long endTime = System.currentTimeMillis();
            System.out.println("🚀 极速统计页面加载完成: " + user.getUsername() +
                " (总耗时: " + (endTime - startTime) + "ms)");

        } catch (Exception e) {
            System.err.println("极速统计页面加载失败: " + e.getMessage());
            e.printStackTrace();
        }

        return "ultra-fast-stats";
    }

    /**
     * 手动触发成就检查的API端点
     */
    @PostMapping("/api/trigger-achievements")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> triggerAchievements(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 1. 同步用户统计数据
            userLevelService.syncUserStats(user);

            // 2. 触发各种成就检查
            achievementService.triggerAchievementCheck(user, "REGISTER", 1);
            achievementService.triggerAchievementCheck(user, "UPLOAD_VIDEO", 1);
            achievementService.triggerAchievementCheck(user, "LIKE_VIDEO", 1);
            achievementService.triggerAchievementCheck(user, "COMMENT", 1);
            achievementService.triggerAchievementCheck(user, "WATCH_VIDEO", 1);
            achievementService.triggerAchievementCheck(user, "LOGIN", 1);

            // 3. 获取更新后的成就进度
            AchievementService.AchievementProgressInfo progressInfo = achievementService.getAchievementProgress(user);

            response.put("success", true);
            response.put("message", "成就检查完成！");
            response.put("totalAchievements", progressInfo.getTotalAchievements());
            response.put("unlockedAchievements", progressInfo.getUnlockedAchievements());
            response.put("completionRate", progressInfo.getCompletionRate());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "成就检查失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 快速修复成就的API端点
     */
    @PostMapping("/api/fix-achievements")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> fixAchievements(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 快速修复成就
            achievementFixService.quickFixUserAchievements(user);

            // 获取成就统计
            String stats = achievementFixService.getUserAchievementStats(user);

            response.put("success", true);
            response.put("message", "成就修复完成！");
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "成就修复失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 极速成就检测API - 0数据库查询
     */
    @PostMapping("/api/auto-detect-achievements")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autoDetectAchievements(Authentication authentication) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 使用模拟检测 - 极速返回
            Map<String, Object> result = ultraFastStatsService.simulateAchievementDetection(user);

            long endTime = System.currentTimeMillis();
            System.out.println("⚡ 极速成就检测完成: " + (endTime - startTime) + "ms");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检测失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user-activity-test")
    public String userActivityTest() {
        return "user-activity-test";
    }

    @GetMapping("/achievement-test")
    public String achievementTest() {
        return "achievement-test";
    }

    @GetMapping("/performance-test")
    public String performanceTest() {
        return "performance-test";
    }

    /**
     * 更新用户个人信息
     */
    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestParam String bio,
                                         @RequestParam String email,
                                         Authentication authentication) {
        try {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setBio(bio);
                user.setEmail(email);
                userService.save(user);
                return ResponseEntity.ok().body(Map.of("success", true, "message", "个人信息更新成功"));
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "更新失败：" + e.getMessage()));
        }
    }

    /**
     * API: 更新用户个人信息 (JSON)
     */
    @PostMapping("/api/user/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfileApi(@RequestBody Map<String, String> request,
                                            Authentication authentication) {
        try {
            System.out.println("收到个人资料更新请求: " + request);

            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "请先登录"));
            }

            String bio = request.get("bio");
            String email = request.get("email");
            String nickname = request.get("nickname");

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("更新用户: " + user.getUsername());

                if (bio != null) {
                    user.setBio(bio);
                    System.out.println("更新bio: " + bio);
                }
                if (email != null) {
                    user.setEmail(email);
                    System.out.println("更新email: " + email);
                }
                if (nickname != null) {
                    user.setNickname(nickname);
                    System.out.println("更新nickname: " + nickname);
                }

                userService.save(user);
                System.out.println("用户信息保存成功");

                return ResponseEntity.ok().body(Map.of("success", true, "message", "个人信息更新成功"));
            }
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "用户不存在"));
        } catch (Exception e) {
            System.err.println("更新个人资料失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "更新失败：" + e.getMessage()));
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/profile/avatar")
    @ResponseBody
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                        Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请选择文件"));
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "只能上传图片文件"));
            }

            // 验证文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文件大小不能超过5MB"));
            }

            // 创建上传目录
            String uploadDir = "src/main/resources/static/uploads/avatars";
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文件名无效"));
            }

            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                fileExtension = ".jpg"; // 默认扩展名
            }
            String fileName = System.currentTimeMillis() + "_" + authentication.getName() + fileExtension;

            // 保存文件
            File destFile = new File(uploadPath, fileName);
            file.transferTo(destFile);

            // 生成访问URL
            String avatarUrl = "/uploads/avatars/" + fileName;

            // 更新用户头像
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setAvatar(avatarUrl);
                userService.save(user);
                return ResponseEntity.ok().body(Map.of("success", true, "message", "头像上传成功", "avatarUrl", avatarUrl));
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户不存在"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "上传失败：" + e.getMessage()));
        }
    }
}