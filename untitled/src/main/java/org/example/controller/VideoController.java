package org.example.controller;

import org.example.dto.VideoContentBlock;
import org.example.entity.Comment;
import org.example.entity.User;
import org.example.entity.Video;
import org.example.entity.VideoContent;
import org.example.service.CommentService;
import org.example.service.UserService;
import org.example.service.VideoService;
import org.example.service.VideoContentService;
import org.example.service.UserLogService;
import org.example.service.NotificationService;
import org.example.service.FollowService;
import org.example.service.AchievementService;
import org.example.repository.VideoLikeRepository;
import org.example.repository.VideoFavoriteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Controller
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoContentService videoContentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserLogService userLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FollowService followService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private VideoFavoriteRepository videoFavoriteRepository;



    @GetMapping("/video/{id}/edit")
    public String editVideo(@PathVariable Long id, Model model, Authentication authentication) {
        // 检查用户是否已登录
        if (authentication == null) {
            return "redirect:/login";
        }
        
        Optional<Video> video = videoService.getVideoById(id);
        if (video.isPresent()) {
            Optional<User> currentUser = userService.findByUsername(authentication.getName());
            if (currentUser.isPresent() && video.get().getUser() != null &&
                    video.get().getUser().getId().equals(currentUser.get().getId())) {
                model.addAttribute("video", video.get());
                return "video/edit";
            }
        }
        return "redirect:/";
    }

    @PostMapping("/video/{id}/edit")
    public String updateVideo(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam String description,
                              @RequestParam(required = false) String tags,
                              @RequestParam(required = false) String url,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) MultipartFile videoFile,
                              @RequestParam(required = false) MultipartFile thumbnailFile,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        // 检查用户是否已登录
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "请先登录");
            return "redirect:/login";
        }
        
        Optional<Video> video = videoService.getVideoById(id);
        if (video.isPresent()) {
            Optional<User> currentUser = userService.findByUsername(authentication.getName());
            if (currentUser.isPresent() && video.get().getUser() != null &&
                    video.get().getUser().getId().equals(currentUser.get().getId())) {

                Video v = video.get();
                v.setTitle(title);
                v.setDescription(description);
                
                // 处理标签
                if (tags != null && !tags.trim().isEmpty()) {
                    v.setTagsString(tags);
                }
                
                // 处理视频URL（只有在没有上传新视频文件时才更新）
                if (url != null && !url.trim().isEmpty() && (videoFile == null || videoFile.isEmpty())) {
                    v.setUrl(url);
                }
                
                // 处理状态
                if (status != null && !status.trim().isEmpty()) {
                    try {
                        v.setStatus(Video.VideoStatus.valueOf(status));
                    } catch (IllegalArgumentException e) {
                        // 如果状态值无效，保持原状态
                    }
                }
                
                // 处理视频文件上传（可选）
                if (videoFile != null && !videoFile.isEmpty()) {
                    try {
                        // 这里应该调用文件上传服务来处理视频文件
                        // 暂时先设置一个占位符URL
                        String videoFileName = System.currentTimeMillis() + "_" + videoFile.getOriginalFilename();
                        v.setUrl("/uploads/videos/" + videoFileName);
                        // TODO: 实际保存文件到服务器
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "视频文件上传失败: " + e.getMessage());
                        return "redirect:/video/" + id + "/edit";
                    }
                }
                
                // 处理缩略图上传（可选）
                if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                    try {
                        // 这里应该调用文件上传服务来处理缩略图
                        // 暂时先设置一个占位符URL
                        String thumbnailFileName = System.currentTimeMillis() + "_" + thumbnailFile.getOriginalFilename();
                        v.setThumbnail("/uploads/thumbnails/" + thumbnailFileName);
                        // TODO: 实际保存文件到服务器
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "缩略图上传失败: " + e.getMessage());
                        return "redirect:/video/" + id + "/edit";
                    }
                }
                
                videoService.saveVideo(v);

                redirectAttributes.addFlashAttribute("success", "视频更新成功！");
                return "redirect:/video/" + id;
            }
        }
        return "redirect:/";
    }

    @PostMapping("/video/{id}/delete")
    public String deleteVideo(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        // 检查用户是否已登录
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "请先登录");
            return "redirect:/login";
        }
        
        Optional<Video> video = videoService.getVideoById(id);
        if (video.isPresent()) {
            Optional<User> currentUser = userService.findByUsername(authentication.getName());
            if (currentUser.isPresent() && video.get().getUser() != null &&
                    video.get().getUser().getId().equals(currentUser.get().getId())) {

                videoService.deleteVideo(id);
                redirectAttributes.addFlashAttribute("success", "视频删除成功！");
                return "redirect:/profile";
            }
        }
        return "redirect:/";
    }

    @PostMapping("/video/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            userLogService.logUserAction("COMMENT_ATTEMPT",
                String.format("User %s attempting to comment on video %d",
                    authentication != null ? authentication.getName() : "anonymous", id));

            // 验证用户认证
            if (authentication == null) {
                userLogService.logError("COMMENT_ERROR", "Unauthenticated comment attempt", null);
                redirectAttributes.addFlashAttribute("error", "请先登录");
                return "redirect:/login";
            }

            // 验证内容
            if (content == null || content.trim().isEmpty()) {
                userLogService.logError("COMMENT_ERROR", "Empty comment content", null);
                redirectAttributes.addFlashAttribute("error", "评论内容不能为空");
                return "redirect:/video/" + id;
            }

            Optional<Video> videoOpt = videoService.getVideoById(id);
            if (!videoOpt.isPresent()) {
                userLogService.logError("COMMENT_ERROR", "Video not found: " + id, null);
                redirectAttributes.addFlashAttribute("error", "视频不存在");
                return "redirect:/";
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                userLogService.logError("COMMENT_ERROR", "User not found: " + authentication.getName(), null);
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/login";
            }

            Comment savedComment = commentService.saveComment(content.trim(), userOpt.get(), videoOpt.get());

            // 发送评论通知
            if (videoOpt.get().getUser() != null) {
                notificationService.createCommentNotification(userOpt.get(), videoOpt.get().getUser(), videoOpt.get(), content.trim());
            }

            userLogService.logUserAction("COMMENT_SUCCESS",
                String.format("Comment created successfully: ID=%d, VideoID=%d, User=%s",
                    savedComment.getId(), id, authentication.getName()));

            // 触发评论成就检查
            try {
                achievementService.triggerAchievementCheck(userOpt.get(), "COMMENT");
                System.out.println("✅ 评论成就检查完成");
            } catch (Exception achievementError) {
                System.err.println("❌ 评论成就检查失败: " + achievementError.getMessage());
            }

            redirectAttributes.addFlashAttribute("success", "评论发表成功");

        } catch (Exception e) {
            userLogService.logError("COMMENT_ERROR",
                String.format("Comment creation failed for user %s on video %d: %s",
                    authentication != null ? authentication.getName() : "anonymous",
                    id, e.getMessage()), e);

            System.err.println("评论发表异常: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "评论发表失败: " + e.getMessage());
        }

        return "redirect:/video/" + id;
    }



    @GetMapping("/popular")
    public String popular(Model model) {
        List<Video> videos = videoService.getPopularVideos();
        model.addAttribute("videos", videos);
        return "popular";
    }

    @GetMapping({"/video/{id}", "/watch/{id}"})
    public String watchVideo(@PathVariable Long id, Model model, Authentication authentication) {
        System.out.println("观看视频，ID: " + id);

        // 临时修复：自动批准所有PENDING状态的视频
        try {
            Optional<Video> videoOpt = videoService.getVideoById(id);
            if (videoOpt.isPresent()) {
                Video video = videoOpt.get();
                if (video.getStatus() == Video.VideoStatus.PENDING) {
                    video.setStatus(Video.VideoStatus.APPROVED);
                    videoService.saveVideo(video);
                    System.out.println("自动批准视频，ID: " + id);
                }
            }
        } catch (Exception e) {
            System.err.println("自动批准视频失败: " + e.getMessage());
        }

        try {
            Optional<Video> video = videoService.getVideoById(id);
            if (video.isPresent()) {
                Video videoEntity = video.get();
                System.out.println("找到视频: " + videoEntity.getTitle() + ", 状态: " + videoEntity.getStatus() + ", 用户: " + (videoEntity.getUser() != null ? videoEntity.getUser().getUsername() : "null"));

                // 检查视频是否被封禁
                if (videoEntity.getStatus() == Video.VideoStatus.BANNED) {
                    // 只有视频所有者可以看到被封禁的视频
                    boolean isOwner = false;
                    if (authentication != null) {
                        Optional<User> currentUser = userService.findByUsername(authentication.getName());
                        if (currentUser.isPresent() && videoEntity.getUser() != null &&
                                videoEntity.getUser().getId().equals(currentUser.get().getId())) {
                            isOwner = true;
                        }
                    }

                    if (!isOwner) {
                        System.out.println("视频已被封禁，ID: " + id);
                        model.addAttribute("error", "视频已被封禁");
                        model.addAttribute("message", "该视频因违反社区规定已被封禁");
                        return "error";
                    }
                }

                // 检查视频是否已审核通过（开放访问权限，允许所有用户查看）
                if (videoEntity.getStatus() != Video.VideoStatus.APPROVED && videoEntity.getStatus() != Video.VideoStatus.BANNED) {
                    boolean isOwner = false;
                    if (authentication != null) {
                        Optional<User> currentUser = userService.findByUsername(authentication.getName());
                        if (currentUser.isPresent() && videoEntity.getUser() != null &&
                                videoEntity.getUser().getId().equals(currentUser.get().getId())) {
                            isOwner = true;
                        }
                    }

                    // 开放访问：允许所有用户查看视频，但显示审核状态提示
                    if (!isOwner) {
                        System.out.println("非所有者访问未审核视频，ID: " + id + ", 状态: " + videoEntity.getStatus());
                        model.addAttribute("videoStatusWarning", "此视频正在审核中");
                    }
                }

                // 注释掉示例视频检查，允许所有视频播放
                /*
                if (videoEntity.getUrl() != null &&
                    (videoEntity.getUrl().contains("placeholder") ||
                     videoEntity.getUrl().contains("sample-videos.com"))) {
                    System.out.println("检测到示例视频数据，ID: " + id);
                    model.addAttribute("error", "该视频为示例数据，无法播放");
                    model.addAttribute("message", "请上传真实的视频文件");
                    return "error";
                }
                */

                // 检查是否是视频所有者
                boolean isOwner = false;
                if (authentication != null) {
                    Optional<User> currentUser = userService.findByUsername(authentication.getName());
                    if (currentUser.isPresent() && videoEntity.getUser() != null &&
                            videoEntity.getUser().getId().equals(currentUser.get().getId())) {
                        isOwner = true;
                    }
                }
                // 总是设置 isOwner 属性，避免模板中出现 null 值
                model.addAttribute("isOwner", isOwner);

                // 只有非所有者观看时才增加观看量
                if (!isOwner) {
                    videoService.incrementViews(id);
                    System.out.println("增加观看量，视频ID: " + id + "，当前观看量: " + (videoEntity.getViews() + 1));

                    // 触发观看成就检查
                    if (authentication != null) {
                        Optional<User> viewerOpt = userService.findByUsername(authentication.getName());
                        if (viewerOpt.isPresent()) {
                            try {
                                achievementService.triggerAchievementCheck(viewerOpt.get(), "WATCH_VIDEO");
                                System.out.println("✅ 观看成就检查完成");
                            } catch (Exception achievementError) {
                                System.err.println("❌ 观看成就检查失败: " + achievementError.getMessage());
                            }
                        }
                    }
                } else {
                    System.out.println("视频所有者观看自己的视频，不增加观看量，视频ID: " + id);
                }

                model.addAttribute("video", videoEntity);

                // 添加当前用户信息
                User currentUser = null;
                if (authentication != null) {
                    Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
                    if (currentUserOpt.isPresent()) {
                        currentUser = currentUserOpt.get();
                        model.addAttribute("currentUser", currentUser);
                    }
                }

                // 检查关注状态
                boolean isFollowing = false;
                if (currentUser != null && videoEntity.getUser() != null &&
                    !currentUser.getId().equals(videoEntity.getUser().getId())) {
                    try {
                        isFollowing = followService.isFollowing(currentUser.getId(), videoEntity.getUser().getId());
                    } catch (Exception e) {
                        System.err.println("检查关注状态失败: " + e.getMessage());
                    }
                }
                model.addAttribute("isFollowing", isFollowing);

                // 检查点赞状态
                boolean isLiked = false;
                if (currentUser != null) {
                    try {
                        isLiked = videoLikeRepository.findByUserAndVideo(currentUser, videoEntity).isPresent();
                    } catch (Exception e) {
                        System.err.println("检查点赞状态失败: " + e.getMessage());
                    }
                }
                model.addAttribute("isLiked", isLiked);

                // 检查收藏状态
                boolean isFavorited = false;
                if (currentUser != null) {
                    try {
                        isFavorited = videoFavoriteRepository.findByUserAndVideo(currentUser, videoEntity).isPresent();
                    } catch (Exception e) {
                        System.err.println("检查收藏状态失败: " + e.getMessage());
                    }
                }
                model.addAttribute("isFavorited", isFavorited);

                // 添加评论数据
                List<Comment> comments = commentService.getCommentsByVideoId(id);
                model.addAttribute("comments", comments);

                // 获取相关视频（暂时使用最新视频作为相关视频）
                List<Video> relatedVideos = videoService.getApprovedVideos().stream()
                    .filter(v -> !v.getId().equals(videoEntity.getId()))
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());
                model.addAttribute("relatedVideos", relatedVideos);

                // 处理富文本内容 - 从video_content表中读取
                List<VideoContent> videoContents = videoContentService.getContentsByVideoId(id);
                if (videoContents != null && !videoContents.isEmpty()) {
                    model.addAttribute("videoContentList", videoContents);
                    System.out.println("从video_content表加载视频内容: " + videoContents.size() + " 个内容块");
                    for (int i = 0; i < videoContents.size(); i++) {
                        VideoContent content = videoContents.get(i);
                        System.out.println("内容块 " + i + ": ID=" + content.getId() +
                            ", 类型=" + content.getType() +
                            ", 数据=" + content.getData() +
                            ", URL=" + content.getUrl());
                    }
                }

                // 处理富文本内容 - 从video.content字段读取（备用）
                if (videoEntity.getContent() != null && !videoEntity.getContent().trim().isEmpty()) {
                    try {
                        System.out.println("原始视频内容JSON: " + videoEntity.getContent());
                        List<VideoContentBlock> videoContentBlocks = objectMapper.readValue(
                            videoEntity.getContent(),
                            new TypeReference<List<VideoContentBlock>>() {}
                        );
                        model.addAttribute("videoContent", videoContentBlocks);
                        System.out.println("加载视频内容: " + videoContentBlocks.size() + " 个内容块");
                        for (int i = 0; i < videoContentBlocks.size(); i++) {
                            VideoContentBlock block = videoContentBlocks.get(i);
                            System.out.println("内容块 " + i + ": 类型=" + block.getType() + ", 数据=" + block.getData() + ", URL=" + block.getUrl());
                        }
                    } catch (Exception e) {
                        System.err.println("解析视频内容失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return "watch-redesigned";
            }
            System.out.println("视频不存在，ID: " + id);
            model.addAttribute("error", "视频不存在");
            model.addAttribute("message", "您访问的视频不存在或已被删除");
            return "error";
        } catch (Exception e) {
            System.err.println("获取视频时发生错误: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "系统错误");
            model.addAttribute("message", "获取视频信息时发生错误: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 测试端点 - 检查视频数据
     */
    @GetMapping("/test/videos")
    @ResponseBody
    public String testVideos() {
        try {
            List<Video> videos = videoService.getAllVideos();
            long count = videos.size();

            StringBuilder sb = new StringBuilder();
            sb.append("视频总数: ").append(count).append("<br>");
            sb.append("视频列表:<br>");

            for (Video video : videos) {
                sb.append("ID: ").append(video.getId())
                  .append(", 标题: ").append(video.getTitle())
                  .append(", 状态: ").append(video.getStatus())
                  .append(", 用户: ").append(video.getUser() != null ? video.getUser().getUsername() : "null")
                  .append("<br>");
            }

            return sb.toString();
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }

}