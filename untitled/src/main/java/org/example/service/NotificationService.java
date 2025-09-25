package org.example.service;

import org.example.entity.Notification;
import org.example.entity.User;
import org.example.entity.Video;
import org.example.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 通知服务
 */
@Service
@Transactional(readOnly = false)
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;



    /**
     * 创建通知
     */
    public Notification createNotification(Long userId, Notification.NotificationType type,
                                         String title, String content) {
        return createNotification(userId, type, title, content, null);
    }

    /**
     * 创建通知（带来源用户）
     */
    public Notification createNotification(Long userId, Notification.NotificationType type,
                                         String title, String content, Long fromUserId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setFromUserId(fromUserId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        // 发送实时通知
        sendRealtimeNotification(saved);

        return saved;
    }

    /**
     * 创建点赞通知
     */
    public void createLikeNotification(User fromUser, User toUser, Video video) {
        if (fromUser.getId().equals(toUser.getId())) {
            return; // 不给自己发通知
        }

        String title = "收到新的点赞";
        String content = String.format("%s 点赞了你的视频《%s》",
                                     fromUser.getUsername(), video.getTitle());

        createNotification(toUser.getId(), Notification.NotificationType.LIKE,
                         title, content, fromUser.getId());
    }

    /**
     * 创建收藏通知
     */
    public void createFavoriteNotification(User fromUser, User toUser, Video video) {
        if (fromUser.getId().equals(toUser.getId())) {
            return; // 不给自己发通知
        }

        String title = "收到新的收藏";
        String content = String.format("%s 收藏了你的视频《%s》",
                                     fromUser.getUsername(), video.getTitle());

        createNotification(toUser.getId(), Notification.NotificationType.FAVORITE,
                         title, content, fromUser.getId());
    }

    /**
     * 创建关注通知
     */
    public void createFollowNotification(User fromUser, User toUser) {
        if (fromUser.getId().equals(toUser.getId())) {
            return; // 不给自己发通知
        }

        String title = "新的关注者";
        String content = String.format("%s 关注了你", fromUser.getUsername());

        createNotification(toUser.getId(), Notification.NotificationType.FOLLOW,
                         title, content, fromUser.getId());
    }

    /**
     * 创建评论通知
     */
    public void createCommentNotification(User fromUser, User toUser, Video video, String commentContent) {
        if (fromUser.getId().equals(toUser.getId())) {
            return; // 不给自己发通知
        }

        String title = "收到新的评论";
        String content = String.format("%s 评论了你的视频《%s》：%s",
                                     fromUser.getUsername(), video.getTitle(),
                                     commentContent.length() > 50 ? commentContent.substring(0, 50) + "..." : commentContent);

        createNotification(toUser.getId(), Notification.NotificationType.COMMENT,
                         title, content, fromUser.getId());
    }

    /**
     * 创建系统通知
     */
    public void createSystemNotification(Long userId, String title, String content) {
        createNotification(userId, Notification.NotificationType.SYSTEM, title, content, null);
    }

    /**
     * 批量创建系统通知（给所有用户）
     */
    public void createSystemNotificationForAll(String title, String content) {
        // 这里可以实现给所有用户发送系统通知的逻辑
        // 为了性能考虑，可以使用异步处理
        System.out.println("系统通知：" + title + " - " + content);
    }

    /**
     * 创建带来源用户的通知
     */
    public Notification createNotification(Long userId, Long fromUserId, 
                                         Notification.NotificationType type, 
                                         String title, String content,
                                         Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        // 发送实时通知
        sendRealtimeNotification(saved);

        return saved;
    }

    /**
     * 获取用户通知列表
     */
    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取用户未读通知数量
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 标记通知为已读
     */
    public void markAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findByIdAndUserId(notificationId, userId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    /**
     * 标记所有通知为已读
     */
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * 删除通知
     */
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.deleteByIdAndUserId(notificationId, userId);
    }

    /**
     * 发送实时通知（已禁用WebSocket）
     */
    private void sendRealtimeNotification(Notification notification) {
        // WebSocket功能已删除，此方法暂时保留但不执行任何操作
        // 可以在这里实现其他实时通知方式，如邮件、短信等
    }

    // === 业务通知方法 ===

    /**
     * 发送评论通知
     */
    public void notifyVideoComment(Video video, User commenter, String commentContent) {
        if (video.getUser() != null && !video.getUser().getId().equals(commenter.getId())) {
            String title = "新评论";
            String content = String.format("%s 评论了您的视频《%s》: %s", 
                commenter.getUsername(), video.getTitle(), 
                commentContent.length() > 50 ? commentContent.substring(0, 50) + "..." : commentContent);
            
            createNotification(
                video.getUser().getId(),
                commenter.getId(),
                Notification.NotificationType.COMMENT,
                title,
                content,
                video.getId(),
                "VIDEO"
            );
        }
    }

    /**
     * 发送点赞通知
     */
    public void notifyVideoLike(Video video, User liker) {
        if (video.getUser() != null && !video.getUser().getId().equals(liker.getId())) {
            String title = "新点赞";
            String content = String.format("%s 点赞了您的视频《%s》", 
                liker.getUsername(), video.getTitle());
            
            createNotification(
                video.getUser().getId(),
                liker.getId(),
                Notification.NotificationType.LIKE,
                title,
                content,
                video.getId(),
                "VIDEO"
            );
        }
    }

    /**
     * 发送收藏通知
     */
    public void notifyVideoFavorite(Video video, User favoriter) {
        if (video.getUser() != null && !video.getUser().getId().equals(favoriter.getId())) {
            String title = "新收藏";
            String content = String.format("%s 收藏了您的视频《%s》", 
                favoriter.getUsername(), video.getTitle());
            
            createNotification(
                video.getUser().getId(),
                favoriter.getId(),
                Notification.NotificationType.FAVORITE,
                title,
                content,
                video.getId(),
                "VIDEO"
            );
        }
    }

    /**
     * 发送关注通知
     */
    public void notifyUserFollow(User followedUser, User follower) {
        String title = "新关注者";
        String content = String.format("%s 关注了您", follower.getUsername());

        createNotification(
            followedUser.getId(),
            follower.getId(),
            Notification.NotificationType.FOLLOW,
            title,
            content,
            follower.getId(),
            "USER"
        );
    }

    /**
     * 发送私信通知
     */
    public void createPrivateMessageNotification(User sender, User receiver, String messageContent) {
        String title = "新私信";
        String content = String.format("%s 给您发送了一条私信", sender.getUsername());

        // 如果消息内容不太长，显示预览
        if (messageContent.length() <= 50) {
            content += ": " + messageContent;
        } else {
            content += ": " + messageContent.substring(0, 47) + "...";
        }

        createNotification(
            receiver.getId(),
            sender.getId(),
            Notification.NotificationType.MESSAGE,
            title,
            content,
            sender.getId(),
            "USER"
        );
    }

    /**
     * 发送视频审核通过通知
     */
    public void notifyVideoApproved(Video video) {
        if (video.getUser() != null) {
            String title = "视频审核通过";
            String content = String.format("您的视频《%s》已审核通过，现在可以被其他用户观看了！", video.getTitle());
            
            createNotification(
                video.getUser().getId(),
                null,
                Notification.NotificationType.VIDEO_APPROVED,
                title,
                content,
                video.getId(),
                "VIDEO"
            );
        }
    }

    /**
     * 发送视频审核拒绝通知
     */
    public void notifyVideoRejected(Video video, String reason) {
        if (video.getUser() != null) {
            String title = "视频审核未通过";
            String content = String.format("您的视频《%s》审核未通过。原因：%s", video.getTitle(), reason);
            
            createNotification(
                video.getUser().getId(),
                null,
                Notification.NotificationType.VIDEO_REJECTED,
                title,
                content,
                video.getId(),
                "VIDEO"
            );
        }
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(Long userId, String title, String content) {
        createNotification(userId, Notification.NotificationType.SYSTEM, title, content);
    }

    /**
     * 发送公告通知
     */
    public void sendAnnouncementNotification(Long userId, String title, String content) {
        createNotification(userId, Notification.NotificationType.ANNOUNCEMENT, title, content);
    }

    /**
     * 创建成就解锁通知
     */
    public void createAchievementNotification(Long userId, String achievementName, String achievementIcon,
                                            Integer points, Long achievementId) {
        String title = "🎉 成就解锁！";
        String content = String.format("恭喜您解锁了成就「%s %s」！获得 %d 积分",
            achievementIcon, achievementName, points);

        createNotification(
            userId,
            null, // 系统通知，无来源用户
            Notification.NotificationType.ACHIEVEMENT,
            title,
            content,
            achievementId,
            "ACHIEVEMENT"
        );
    }

    /**
     * 批量创建成就解锁通知
     */
    public void createBatchAchievementNotifications(Long userId, List<Map<String, Object>> achievements) {
        for (Map<String, Object> achievement : achievements) {
            createAchievementNotification(
                userId,
                (String) achievement.get("name"),
                (String) achievement.get("icon"),
                (Integer) achievement.get("points"),
                (Long) achievement.get("id")
            );
        }
    }

    /**
     * 批量发送公告通知
     */
    public void broadcastAnnouncement(List<Long> userIds, String title, String content) {
        for (Long userId : userIds) {
            sendAnnouncementNotification(userId, title, content);
        }
    }

    /**
     * 获取未读通知数量
     */
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 获取最新通知
     */
    public List<Notification> getRecentNotifications(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return page.getContent();
    }

    /**
     * 标记所有通知为已读
     */
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * 发送好友请求通知
     */
    public void sendFriendRequestNotification(User receiver, User sender) {
        String message = sender.getUsername() + " 向您发送了好友请求";
        createNotification(
            receiver.getId(),
            sender.getId(),
            Notification.NotificationType.FRIEND_REQUEST,
            "好友请求",
            message,
            sender.getId(),
            "USER"
        );
    }

    /**
     * 发送好友接受通知
     */
    public void sendFriendAcceptedNotification(User receiver, User sender) {
        String message = sender.getUsername() + " 接受了您的好友请求";
        createNotification(
            receiver.getId(),
            sender.getId(),
            Notification.NotificationType.FRIEND_ACCEPTED,
            "好友请求",
            message,
            sender.getId(),
            "USER"
        );
    }

    /**
     * 发送消息通知
     */
    public void sendMessageNotification(User receiver, User sender, Object conversation, Object message) {
        String notificationMessage = sender.getUsername() + " 给您发送了一条消息";
        createNotification(
            receiver.getId(),
            sender.getId(),
            Notification.NotificationType.MESSAGE,
            "新消息",
            notificationMessage,
            sender.getId(),
            "USER"
        );
    }
}
