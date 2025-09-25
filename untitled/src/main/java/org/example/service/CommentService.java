package org.example.service;

import org.example.entity.Comment;
import org.example.entity.User;
import org.example.repository.CommentRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = false)
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserRepository userRepository;



    public long getTotalComments() {
        return commentRepository.count();
    }

    public long getPendingCommentsCount() {
        return commentRepository.countByStatus(Comment.CommentStatus.PENDING);
    }

    public List<Comment> getPendingComments() {
        return commentRepository.findByStatus(Comment.CommentStatus.PENDING);
    }

    public Page<Comment> getAllCommentsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findAll(pageable);
    }

    public List<Comment> getCommentsByVideoId(Long videoId) {
        return commentRepository.findByVideoId(videoId)
                .stream()
                .filter(comment -> comment.getStatus() == Comment.CommentStatus.APPROVED)
                .collect(java.util.stream.Collectors.toList());
    }

    public Comment createComment(Long videoId, Long userId, String username, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setStatus(Comment.CommentStatus.APPROVED); // 默认通过，可以改为PENDING需要审核

        Comment savedComment = commentRepository.save(comment);

        // 触发评论成就检查
        try {
            userRepository.findById(userId).ifPresent(user -> {
                achievementService.triggerAchievementCheck(user, "COMMENT", 1);
            });
        } catch (Exception e) {
            System.err.println("❌ 评论成就检查失败: " + e.getMessage());
        }

        return savedComment;
    }

    public void approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        comment.setStatus(Comment.CommentStatus.APPROVED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public void rejectComment(Long commentId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        comment.setStatus(Comment.CommentStatus.REJECTED);
        comment.setBannedReason(reason);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public void banComment(Long commentId, String reason, String adminUsername) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        comment.setStatus(Comment.CommentStatus.BANNED);
        comment.setBannedReason(reason);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        commentRepository.delete(comment);
    }

    public void likeComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    public List<Comment> getCommentsByUserId(Long userId) {
        return commentRepository.findByUserId(userId);
    }

    public List<Comment> getReplies(Long parentId) {
        return commentRepository.findByParentId(parentId);
    }
    // 在CommentService中添加缺失方法

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
    }

    // 添加缺失的saveComment方法
    public Comment saveComment(String content, org.example.entity.User user, org.example.entity.Video video) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUserId(user.getId());
        comment.setUsername(user.getUsername());
        comment.setVideoId(video.getId());
        comment.setStatus(Comment.CommentStatus.APPROVED); // 默认通过审核
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        // 触发评论成就检查
        try {
            achievementService.triggerAchievementCheck(user, "COMMENT", 1);
        } catch (Exception e) {
            System.err.println("❌ 评论成就检查失败: " + e.getMessage());
        }

        return savedComment;
    }

    // 保存评论对象
    public Comment saveComment(Comment comment) {
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        if (comment.getStatus() == null) {
            comment.setStatus(Comment.CommentStatus.APPROVED);
        }
        Comment savedComment = commentRepository.save(comment);

        // 触发评论成就检查
        try {
            userRepository.findById(comment.getUserId()).ifPresent(user -> {
                achievementService.triggerAchievementCheck(user, "COMMENT", 1);
            });
        } catch (Exception e) {
            System.err.println("❌ 评论成就检查失败: " + e.getMessage());
        }

        return savedComment;
    }

    /**
     * 删除视频的所有评论
     */
    @Transactional
    public void deleteByVideoId(Long videoId) {
        commentRepository.deleteByVideoId(videoId);
    }

}