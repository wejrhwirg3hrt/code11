package org.example.controller;

import org.example.dto.RealtimeCommentMessage;
import org.example.entity.Comment;
import org.example.entity.User;
import org.example.service.CommentService;
import org.example.service.UserService;
import org.example.service.AchievementService;
import org.example.service.UserLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 实时评论控制器
 * 处理WebSocket实时评论功能
 */
@Controller
public class RealtimeCommentController {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeCommentController.class);

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private WebRTCSignalingController webRTCSignalingController;

    @Autowired
    private org.example.service.UserOnlineStatusService userOnlineStatusService;

    /**
     * 处理实时评论消息
     */
    @MessageMapping("/comment.send")
    public void sendComment(@Payload RealtimeCommentMessage message, 
                           SimpMessageHeaderAccessor headerAccessor,
                           Principal principal) {
        try {
            // 验证用户身份
            if (principal == null) {
                return;
            }

            String username = principal.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return;
            }

            User user = userOpt.get();

            // 保存评论到数据库
            Comment comment = new Comment();
            comment.setContent(message.getContent());
            comment.setVideoId(message.getVideoId());
            comment.setUserId(user.getId());
            comment.setCreatedAt(LocalDateTime.now());
            
            Comment savedComment = commentService.saveComment(comment);

            // 触发成就检查 - 发表评论
            try {
                achievementService.triggerAchievementCheck(user, "COMMENT");
                System.out.println("✅ 评论成就检查完成: " + user.getUsername());
            } catch (Exception e) {
                System.err.println("❌ 评论成就检查失败: " + e.getMessage());
            }

            // 创建实时消息
            RealtimeCommentMessage realtimeMessage = new RealtimeCommentMessage();
            realtimeMessage.setId(savedComment.getId());
            realtimeMessage.setContent(savedComment.getContent());
            realtimeMessage.setVideoId(savedComment.getVideoId());
            realtimeMessage.setUsername(user.getUsername());
            realtimeMessage.setUserAvatar(user.getAvatar());
            realtimeMessage.setCreatedAt(savedComment.getCreatedAt());
            realtimeMessage.setType("COMMENT");

            // WebSocket功能已删除，实时评论广播功能暂时禁用

            System.out.println("实时评论发送成功: " + message.getContent());

        } catch (Exception e) {
            System.err.println("处理实时评论失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理弹幕消息
     */
    @MessageMapping("/danmaku.send")
    public void sendDanmaku(@Payload RealtimeCommentMessage message,
                           Principal principal) {
        try {
            if (principal == null) {
                return;
            }

            String username = principal.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return;
            }

            User user = userOpt.get();

            // 创建弹幕消息
            RealtimeCommentMessage danmakuMessage = new RealtimeCommentMessage();
            danmakuMessage.setContent(message.getContent());
            danmakuMessage.setVideoId(message.getVideoId());
            danmakuMessage.setUsername(user.getUsername());
            danmakuMessage.setUserAvatar(user.getAvatar());
            danmakuMessage.setTime(message.getTime());
            danmakuMessage.setCreatedAt(LocalDateTime.now());
            danmakuMessage.setType("DANMAKU");

            // WebSocket功能已删除，弹幕广播功能暂时禁用

            System.out.println("弹幕发送成功: " + message.getContent());

        } catch (Exception e) {
            System.err.println("处理弹幕失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 用户加入视频观看
     */
    @MessageMapping("/video.join")
    public void joinVideo(@Payload RealtimeCommentMessage message,
                         Principal principal) {
        try {
            if (principal == null) {
                return;
            }

            String username = principal.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return;
            }

            User user = userOpt.get();

            // 创建加入消息
            RealtimeCommentMessage joinMessage = new RealtimeCommentMessage();
            joinMessage.setVideoId(message.getVideoId());
            joinMessage.setUsername(user.getUsername());
            joinMessage.setUserAvatar(user.getAvatar());
            joinMessage.setCreatedAt(LocalDateTime.now());
            joinMessage.setType("USER_JOIN");
            joinMessage.setContent(user.getUsername() + " 加入了观看");

            // WebSocket功能已删除，用户活动广播功能暂时禁用

            System.out.println("用户加入视频观看: " + username);

        } catch (Exception e) {
            System.err.println("处理用户加入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 用户离开视频观看
     */
    @MessageMapping("/video.leave")
    public void leaveVideo(@Payload RealtimeCommentMessage message,
                          Principal principal) {
        try {
            if (principal == null) {
                return;
            }

            String username = principal.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return;
            }

            User user = userOpt.get();

            // 创建离开消息
            RealtimeCommentMessage leaveMessage = new RealtimeCommentMessage();
            leaveMessage.setVideoId(message.getVideoId());
            leaveMessage.setUsername(user.getUsername());
            leaveMessage.setUserAvatar(user.getAvatar());
            leaveMessage.setCreatedAt(LocalDateTime.now());
            leaveMessage.setType("USER_LEAVE");
            leaveMessage.setContent(user.getUsername() + " 离开了观看");

            // WebSocket功能已删除，用户活动广播功能暂时禁用

            System.out.println("用户离开视频观看: " + username);

        } catch (Exception e) {
            System.err.println("处理用户离开失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * REST API - 获取在线观看人数
     */
    @GetMapping("/api/video/{videoId}/viewers")
    @ResponseBody
    public int getViewerCount(@PathVariable Long videoId) {
        // 从WebRTC信令控制器获取在线用户数
        try {
            // 这里可以进一步优化，根据具体视频获取观看人数
            // 目前返回总在线用户数作为观看人数的估算
            return userOnlineStatusService.getOnlineUserCount();
        } catch (Exception e) {
            logger.error("获取在线观看人数失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * REST API - 获取视频弹幕
     */
    @GetMapping("/api/video/{videoId}/danmaku")
    @ResponseBody
    public Object getDanmaku(@PathVariable Long videoId) {
        // 这里可以实现获取视频弹幕的逻辑
        // 从数据库或缓存中获取弹幕数据
        return "[]"; // 暂时返回空数组
    }
}
