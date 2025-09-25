package org.example.service;

import java.util.List;

import org.example.entity.Conversation;
import org.example.entity.Music;
import org.example.entity.User;
import org.example.entity.UserAchievement;
import org.example.entity.UserFollow;
import org.example.entity.Video;
import org.example.entity.VideoLike;
import org.example.repository.CommentRepository;
import org.example.repository.ConversationParticipantRepository;
import org.example.repository.ConversationRepository;
import org.example.repository.MessageRepository;
import org.example.repository.MusicRepository;
import org.example.repository.UserAchievementRepository;
import org.example.repository.UserFollowRepository;
import org.example.repository.UserLevelRepository;
import org.example.repository.UserLogRepository;
import org.example.repository.UserLoginLogRepository;
import org.example.repository.UserMomentRepository;
import org.example.repository.UserRepository;
import org.example.repository.VideoContentRepository;
import org.example.repository.VideoFavoriteRepository;
import org.example.repository.VideoLikeRepository;
import org.example.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDataCleanupService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserLevelRepository userLevelRepository;
    
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    
    @Autowired
    private UserLogRepository userLogRepository;
    
    @Autowired
    private UserLoginLogRepository userLoginLogRepository;
    
    @Autowired
    private UserMomentRepository userMomentRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private MusicRepository musicRepository;
    
    @Autowired
    private VideoLikeRepository videoLikeRepository;
    
    @Autowired
    private UserFollowRepository userFollowRepository;
    
    @Autowired
    private VideoContentRepository videoContentRepository;
    
    @Autowired
    private VideoFavoriteRepository videoFavoriteRepository;
    
    @Autowired
    private ConversationParticipantRepository conversationParticipantRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ConversationRepository conversationRepository;

    /**
     * 清空所有普通用户数据，保留管理员和root用户
     */
    @Transactional
    public void cleanupAllUserData() {
        System.out.println("开始清空用户数据...");
        
        try {
            // 1. 获取所有普通用户（非管理员）
            List<User> regularUsers = userRepository.findByRoleNotIn(List.of("ADMIN", "SUPER_ADMIN"));
            System.out.println("找到 " + regularUsers.size() + " 个普通用户需要清理");
            
            // 2. 获取管理员用户列表（用于保留）
            List<User> adminUsers = userRepository.findByRoleIn(List.of("ADMIN", "SUPER_ADMIN"));
            System.out.println("保留 " + adminUsers.size() + " 个管理员用户");
            
            // 3. 清理普通用户的相关数据
            for (User user : regularUsers) {
                cleanupUserData(user.getId());
            }
            
            // 4. 删除普通用户账号
            userRepository.deleteAll(regularUsers);
            
            System.out.println("用户数据清理完成！");
            System.out.println("保留的管理员用户:");
            for (User admin : adminUsers) {
                System.out.println("- " + admin.getUsername() + " (角色: " + admin.getRole() + ")");
            }
            
        } catch (Exception e) {
            System.err.println("清理用户数据时出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("清理用户数据失败", e);
        }
    }

    /**
     * 清理指定用户的所有相关数据
     */
    @Transactional
    public void cleanupUserData(Long userId) {
        System.out.println("清理用户ID " + userId + " 的数据...");
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.out.println("用户ID " + userId + " 不存在，跳过清理");
                return;
            }
            
            // 1. 删除用户等级数据
            userLevelRepository.findByUserId(userId).ifPresent(userLevelRepository::delete);
            
            // 2. 删除用户成就数据
            List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
            userAchievementRepository.deleteAll(userAchievements);
            
            // 3. 删除用户日志数据
            userLogRepository.deleteByUserId(userId);
            
            // 4. 删除用户登录日志
            userLoginLogRepository.deleteByUserId(userId);
            
            // 5. 删除用户动态
            userMomentRepository.deleteByUser_Id(userId);
            
            // 6. 删除用户评论
            commentRepository.deleteByUserId(userId);
            
            // 7. 删除用户上传的视频
            List<Video> userVideos = videoRepository.findByUserId(userId);
            for (Video video : userVideos) {
                // 删除视频内容（必须在删除视频之前）
                videoContentRepository.deleteByVideoId(video.getId());
                // 删除视频相关的点赞
                videoLikeRepository.deleteByVideoId(video.getId());
                // 删除视频相关的收藏
                videoFavoriteRepository.deleteByVideoId(video.getId());
                // 删除视频相关的评论
                commentRepository.deleteByVideoId(video.getId());
            }
            videoRepository.deleteAll(userVideos);
            
            // 8. 删除用户上传的音乐
            List<Music> userMusic = musicRepository.findByUserIdOrderByUploadTimeDesc(userId);
            musicRepository.deleteAll(userMusic);
            
            // 9. 删除用户的点赞记录
            List<VideoLike> userLikes = videoLikeRepository.findByUserIdOrderByCreatedAtDesc(userId);
            videoLikeRepository.deleteAll(userLikes);
            
            // 10. 删除用户的关注关系
            List<UserFollow> userFollows = userFollowRepository.findByFollowerId(userId);
            userFollowRepository.deleteAll(userFollows);
            
            List<UserFollow> userFollowers = userFollowRepository.findByFollowingId(userId);
            userFollowRepository.deleteAll(userFollowers);
            
            // 11. 清理聊天系统相关数据
            // 删除用户发送的消息（外键约束应该自动删除相关的附件和已读状态）
            messageRepository.deleteBySenderId(userId);
            
            // 删除会话参与者记录
            conversationParticipantRepository.deleteByUserId(userId);
            
            // 删除用户创建的会话（如果会话中没有其他参与者）
            List<Conversation> userConversations = conversationRepository.findByCreatedByAndIsActiveTrue(user);
            for (Conversation conversation : userConversations) {
                // 检查会话是否还有其他参与者
                long participantCount = conversationParticipantRepository.countActiveParticipants(conversation);
                if (participantCount <= 1) {
                    // 如果只有当前用户，删除整个会话
                    conversationRepository.delete(conversation);
                }
            }
            
            System.out.println("用户ID " + userId + " 的数据清理完成");
            
        } catch (Exception e) {
            System.err.println("清理用户ID " + userId + " 数据时出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("清理用户数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取清理统计信息
     */
    public String getCleanupStatistics() {
        StringBuilder stats = new StringBuilder();
        
        long totalUsers = userRepository.count();
        long adminUsers = userRepository.countByRoleIn(List.of("ADMIN", "SUPER_ADMIN"));
        long regularUsers = totalUsers - adminUsers;
        
        stats.append("=== 用户数据统计 ===\n");
        stats.append("总用户数: ").append(totalUsers).append("\n");
        stats.append("管理员用户: ").append(adminUsers).append("\n");
        stats.append("普通用户: ").append(regularUsers).append("\n");
        stats.append("用户等级数据: ").append(userLevelRepository.count()).append("\n");
        stats.append("用户成就数据: ").append(userAchievementRepository.count()).append("\n");
        stats.append("用户日志数据: ").append(userLogRepository.count()).append("\n");
        stats.append("用户登录日志: ").append(userLoginLogRepository.count()).append("\n");
        stats.append("用户动态: ").append(userMomentRepository.count()).append("\n");
        stats.append("评论数据: ").append(commentRepository.count()).append("\n");
        stats.append("视频数据: ").append(videoRepository.count()).append("\n");
        stats.append("音乐数据: ").append(musicRepository.count()).append("\n");
        stats.append("点赞数据: ").append(videoLikeRepository.count()).append("\n");
        stats.append("关注数据: ").append(userFollowRepository.count()).append("\n");
        
        return stats.toString();
    }

    /**
     * 安全清理 - 只清理已删除的用户数据
     */
    @Transactional
    public void cleanupDeletedUserData() {
        System.out.println("开始清理已删除用户的数据...");
        
        try {
            // 获取所有已删除的用户
            List<User> deletedUsers = userRepository.findByDeletedTrue();
            System.out.println("找到 " + deletedUsers.size() + " 个已删除用户需要清理");
            
            for (User user : deletedUsers) {
                cleanupUserData(user.getId());
            }
            
            // 删除已删除的用户账号
            userRepository.deleteAll(deletedUsers);
            
            System.out.println("已删除用户数据清理完成！");
            
        } catch (Exception e) {
            System.err.println("清理已删除用户数据时出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("清理已删除用户数据失败", e);
        }
    }
} 