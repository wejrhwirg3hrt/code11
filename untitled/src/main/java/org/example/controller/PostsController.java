package org.example.controller;

import org.example.entity.User;
import org.example.entity.UserMoment;
import org.example.entity.MomentComment;
import org.example.entity.MomentLike;
import org.example.service.UserMomentService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/posts")
public class PostsController {

    @Autowired
    private UserMomentService momentService;

    @Autowired
    private UserService userService;

    /**
     * 动态主页
     */
    @GetMapping
    public String momentsHome(Model model, Authentication auth,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {

        Page<UserMoment> momentsPage;
        User currentUser = null;

        if (auth != null && auth.isAuthenticated()) {
            currentUser = userService.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                // 获取关注用户的动态
                momentsPage = momentService.getFollowingMoments(currentUser.getId(), page, size);
                model.addAttribute("currentUser", currentUser);
            } else {
                // 获取公开动态
                momentsPage = momentService.getPublicMoments(page, size);
            }
        } else {
            // 获取公开动态
            momentsPage = momentService.getPublicMoments(page, size);
        }

        model.addAttribute("momentsPage", momentsPage);

        // 如果用户已登录，获取用户的点赞状态
        if (currentUser != null) {
            Map<Long, Boolean> likedStatus = new HashMap<>();
            for (UserMoment moment : momentsPage.getContent()) {
                boolean isLiked = momentService.isLikedByUser(moment.getId(), currentUser.getId());
                likedStatus.put(moment.getId(), isLiked);
            }
            model.addAttribute("likedStatus", likedStatus);
        }

        // 获取每个动态的点赞用户信息（最多显示30个）
        Map<Long, List<MomentLike>> likesInfo = new HashMap<>();
        for (UserMoment moment : momentsPage.getContent()) {
            List<MomentLike> likes = momentService.getMomentLikes(moment.getId(), 30);
            likesInfo.put(moment.getId(), likes);
        }
        model.addAttribute("likesInfo", likesInfo);

        return "posts/index";
    }

    /**
     * 发布动态页面
     */
    @GetMapping("/publish")
    public String publishPage(Authentication auth, Model model) {
        // 临时移除权限检查，用于调试
        System.out.println("=== 访问 /posts/publish ===");
        System.out.println("Authentication: " + auth);
        if (auth != null) {
            System.out.println("Authenticated: " + auth.isAuthenticated());
            System.out.println("Username: " + auth.getName());
        }

        // 如果用户未登录，添加提示信息但仍然显示页面
        if (auth == null || !auth.isAuthenticated()) {
            model.addAttribute("needLogin", true);
            model.addAttribute("message", "请先登录后再发布动态");
        }

        return "posts/publish";
    }

    /**
     * 处理发布动态
     */
    @PostMapping("/publish")
    public String publishMoment(@RequestParam("content") String content,
                              @RequestParam(value = "images", required = false) MultipartFile[] images,
                              @RequestParam(value = "location", required = false) String location,
                              @RequestParam(value = "mood", required = false) String mood,
                              @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/posts/publish"; // 留在发布页面
            }

            List<MultipartFile> imageList = images != null ? Arrays.asList(images) : null;
            UserMoment moment = momentService.publishMoment(user.getId(), content, imageList, location, mood, isPublic);

            redirectAttributes.addFlashAttribute("success", "动态发布成功！");
            return "redirect:/posts"; // 成功后跳转到动态广场

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "发布失败：" + e.getMessage());
            return "redirect:/posts/publish"; // 失败时留在发布页面
        }
    }

    /**
     * 动态详情页面
     */
    @GetMapping("/{momentId}")
    public String momentDetail(@PathVariable Long momentId, Model model, Authentication auth) {
        Optional<UserMoment> momentOpt = momentService.getMomentById(momentId);
        if (!momentOpt.isPresent()) {
            return "redirect:/posts";
        }

        UserMoment moment = momentOpt.get();
        model.addAttribute("moment", moment);

        // 解析图片路径
        List<String> imagePaths = momentService.parseImagePaths(moment.getImages());
        model.addAttribute("imagePaths", imagePaths);

        // 获取评论
        List<MomentComment> comments = momentService.getMomentComments(momentId);
        model.addAttribute("comments", comments);

        // 动态作者信息已经通过预加载的user对象获取
        model.addAttribute("author", moment.getUser());

        // 获取点赞用户信息（最多显示30个）
        List<MomentLike> likes = momentService.getMomentLikes(momentId, 30);
        model.addAttribute("likes", likes);

        // 当前用户信息
        if (auth != null && auth.isAuthenticated()) {
            User currentUser = userService.findByUsername(auth.getName()).orElse(null);
            model.addAttribute("currentUser", currentUser);

            // 检查当前用户是否已点赞
            if (currentUser != null) {
                boolean isLiked = momentService.isLikedByUser(momentId, currentUser.getId());
                model.addAttribute("isLiked", isLiked);
            }
        }

        return "posts/detail";
    }

    /**
     * 我的动态页面
     */
    @GetMapping("/my")
    public String myMoments(Model model, Authentication auth,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Page<UserMoment> momentsPage = momentService.getUserMoments(user.getId(), page, size);
        model.addAttribute("momentsPage", momentsPage);
        model.addAttribute("currentUser", user);

        // 获取用户的点赞状态
        Map<Long, Boolean> likedStatus = new HashMap<>();
        for (UserMoment moment : momentsPage.getContent()) {
            boolean isLiked = momentService.isLikedByUser(moment.getId(), user.getId());
            likedStatus.put(moment.getId(), isLiked);
        }
        model.addAttribute("likedStatus", likedStatus);

        return "posts/my-posts";
    }

    /**
     * 点赞/取消点赞动态
     */
    @PostMapping("/{momentId}/like")
    @ResponseBody
    public String likeMoment(@PathVariable Long momentId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "{\"success\": false, \"message\": \"用户不存在\"}";
        }

        boolean wasLiked = momentService.isLikedByUser(momentId, user.getId());
        boolean success = momentService.likeMoment(momentId, user.getId());

        if (success) {
            // 获取更新后的动态信息
            Optional<UserMoment> momentOpt = momentService.findById(momentId);
            if (momentOpt.isPresent()) {
                UserMoment moment = momentOpt.get();
                String action = wasLiked ? "取消点赞" : "点赞";
                return "{\"success\": true, \"message\": \"" + action + "成功\", " +
                       "\"liked\": " + !wasLiked + ", " +
                       "\"likeCount\": " + moment.getLikeCount() + "}";
            }
        }

        return "{\"success\": false, \"message\": \"操作失败\"}";
    }

    /**
     * 评论动态
     */
    @PostMapping("/{momentId}/comment")
    @ResponseBody
    public String commentMoment(@PathVariable Long momentId,
                              @RequestParam("content") String content,
                              @RequestParam(value = "replyToUserId", required = false) Long replyToUserId,
                              @RequestParam(value = "replyToCommentId", required = false) Long replyToCommentId,
                              Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "{\"success\": false, \"message\": \"用户不存在\"}";
        }

        try {
            MomentComment comment = momentService.commentMoment(momentId, user.getId(), content, replyToUserId, replyToCommentId);
            return "{\"success\": true, \"message\": \"评论成功\", \"commentId\": " + comment.getId() + "}";
        } catch (RuntimeException e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 获取动态评论列表 API
     */
    @GetMapping("/api/posts/{momentId}/comments")
    @ResponseBody
    public String getMomentCommentsApi(@PathVariable Long momentId) {
        try {
            List<MomentComment> comments = momentService.getMomentComments(momentId);

            StringBuilder json = new StringBuilder();
            json.append("{\"success\": true, \"comments\": [");

            for (int i = 0; i < comments.size(); i++) {
                MomentComment comment = comments.get(i);
                Optional<User> userOpt = userService.findById(comment.getUserId());

                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"id\": ").append(comment.getId()).append(",");
                json.append("\"content\": \"").append(comment.getContent().replace("\"", "\\\"")).append("\",");
                json.append("\"username\": \"").append(userOpt.map(User::getUsername).orElse("未知用户")).append("\",");
                json.append("\"userAvatar\": \"").append(userOpt.map(User::getAvatar).orElse("/images/default-avatar.png")).append("\",");
                json.append("\"createTime\": \"").append(comment.getCreateTime()).append("\"");
                json.append("}");
            }

            json.append("]}");
            return json.toString();
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"获取评论失败\"}";
        }
    }

    /**
     * 删除动态
     */
    @PostMapping("/delete/{momentId}")
    @ResponseBody
    public String deleteMoment(@PathVariable Long momentId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "{\"success\": false, \"message\": \"用户不存在\"}";
        }

        boolean success = momentService.deleteMoment(momentId, user.getId());
        if (success) {
            return "{\"success\": true, \"message\": \"删除成功\"}";
        } else {
            return "{\"success\": false, \"message\": \"删除失败或无权限\"}";
        }
    }

    /**
     * 搜索动态
     */
    @GetMapping("/search")
    public String searchMoments(@RequestParam("q") String keyword, Model model) {
        List<UserMoment> searchResults = momentService.searchMoments(keyword);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);
        return "posts/search";
    }


}
