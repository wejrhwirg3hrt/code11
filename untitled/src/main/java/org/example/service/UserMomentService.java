package org.example.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.entity.MomentComment;
import org.example.entity.MomentLike;
import org.example.entity.User;
import org.example.entity.UserMoment;
import org.example.repository.MomentCommentRepository;
import org.example.repository.MomentLikeRepository;
import org.example.repository.UserFollowRepository;
import org.example.repository.UserMomentRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = false)
public class UserMomentService {

    @Autowired
    private UserMomentRepository momentRepository;

    @Autowired
    private MomentCommentRepository commentRepository;

    @Autowired
    private MomentLikeRepository likeRepository;

    @Autowired
    private UserFollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String MOMENT_IMAGE_DIR = "uploads/moments/";

    /**
     * 发布动态
     */
    public UserMoment publishMoment(Long userId, String content, List<MultipartFile> images,
                                  String location, String mood, Boolean isPublic) throws IOException {

        // 获取用户对象
        Optional<User> userOpt = userService.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        User user = userOpt.get();

        // 处理图片上传
        List<String> imagePaths = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            // 创建目录
            Path imageDir = Paths.get(MOMENT_IMAGE_DIR);
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
            }

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                    String imagePath = MOMENT_IMAGE_DIR + fileName;

                    Path targetPath = imageDir.resolve(fileName);
                    Files.copy(image.getInputStream(), targetPath);

                    imagePaths.add(imagePath);
                }
            }
        }

        // 创建动态
        UserMoment moment = new UserMoment();
        moment.setUser(user);
        moment.setContent(content);
        moment.setLocation(location);
        moment.setMood(mood);
        moment.setCreateTime(LocalDateTime.now());
        moment.setLikeCount(0L);
        moment.setCommentCount(0L);
        moment.setIsPublic(isPublic != null ? isPublic : true);

        // 将图片路径转换为JSON字符串
        if (!imagePaths.isEmpty()) {
            try {
                moment.setImages(objectMapper.writeValueAsString(imagePaths));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return momentRepository.save(moment);
    }

    /**
     * 获取用户动态
     */
    public Page<UserMoment> getUserMoments(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return momentRepository.findByUser_IdOrderByCreateTimeDesc(userId, pageable);
    }

    /**
     * 获取公开动态
     */
    public Page<UserMoment> getPublicMoments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return momentRepository.findByIsPublicTrueOrderByCreateTimeDesc(pageable);
    }

    /**
     * 获取关注用户的动态
     */
    public Page<UserMoment> getFollowingMoments(Long userId, int page, int size) {
        // 获取关注的用户ID列表
        List<Long> followingIds = followRepository.findByFollowerIdAndAcceptedTrue(userId)
                .stream()
                .map(follow -> follow.getFollowing().getId())
                .collect(Collectors.toList());

        // 添加自己的ID
        followingIds.add(userId);

        Pageable pageable = PageRequest.of(page, size);
        return momentRepository.findByUserIdInAndIsPublicTrueOrderByCreateTimeDesc(followingIds, pageable);
    }

    /**
     * 点赞/取消点赞动态
     */
    public boolean likeMoment(Long momentId, Long userId) {
        Optional<UserMoment> momentOpt = findById(momentId);
        if (!momentOpt.isPresent()) {
            return false;
        }

        UserMoment moment = momentOpt.get();
        Optional<MomentLike> existingLike = likeRepository.findByMomentIdAndUserId(momentId, userId);

        if (existingLike.isPresent()) {
            // 已经点赞，取消点赞
            likeRepository.delete(existingLike.get());
            moment.setLikeCount(Math.max(0, moment.getLikeCount() - 1));
        } else {
            // 未点赞，添加点赞
            MomentLike like = new MomentLike(momentId, userId);
            likeRepository.save(like);
            moment.setLikeCount(moment.getLikeCount() + 1);
        }

        momentRepository.save(moment);
        return true;
    }

    /**
     * 检查用户是否已点赞动态
     */
    public boolean isLikedByUser(Long momentId, Long userId) {
        return likeRepository.existsByMomentIdAndUserId(momentId, userId);
    }

    /**
     * 获取动态的点赞用户列表
     */
    public List<MomentLike> getMomentLikes(Long momentId) {
        return likeRepository.findByMomentIdOrderByCreateTimeDesc(momentId);
    }

    /**
     * 获取动态的点赞用户列表（带用户信息，限制数量）
     */
    public List<MomentLike> getMomentLikes(Long momentId, int limit) {
        List<MomentLike> likes = likeRepository.findByMomentIdWithUserOrderByCreateTimeDesc(momentId);
        return likes.size() > limit ? likes.subList(0, limit) : likes;
    }

    /**
     * 根据ID查找动态
     */
    public Optional<UserMoment> findById(Long momentId) {
        UserMoment moment = momentRepository.findByIdWithUser(momentId);
        return Optional.ofNullable(moment);
    }

    /**
     * 评论动态
     */
    public MomentComment commentMoment(Long momentId, Long userId, String content,
                                     Long replyToUserId, Long replyToCommentId) {
        // 验证动态是否存在
        Optional<UserMoment> momentOpt = findById(momentId);
        if (!momentOpt.isPresent()) {
            throw new RuntimeException("动态不存在");
        }

        MomentComment comment = new MomentComment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setReplyToUserId(replyToUserId);
        comment.setReplyToCommentId(replyToCommentId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setLikeCount(0L);

        MomentComment savedComment = commentRepository.save(comment);

        // 更新动态的评论数量
        UserMoment moment = momentOpt.get();
        moment.setCommentCount(moment.getCommentCount() + 1);
        momentRepository.save(moment);

        return savedComment;
    }

    /**
     * 获取动态评论
     */
    public List<MomentComment> getMomentComments(Long momentId) {
        List<MomentComment> comments = commentRepository.findByMomentIdOrderByCreateTimeAsc(momentId);

        // 为每个评论加载用户信息
        for (MomentComment comment : comments) {
            Optional<User> userOpt = userRepository.findById(comment.getUserId());
            if (userOpt.isPresent()) {
                comment.setUser(userOpt.get());
            }
        }

        return comments;
    }

    /**
     * 删除动态
     */
    public boolean deleteMoment(Long momentId, Long userId) {
        Optional<UserMoment> momentOpt = findById(momentId);
        if (momentOpt.isPresent()) {
            UserMoment moment = momentOpt.get();

            // 验证权限
            if (!moment.getUser().getId().equals(userId)) {
                return false;
            }

            // 删除图片文件
            if (moment.getImages() != null) {
                try {
                    List<String> imagePaths = objectMapper.readValue(moment.getImages(), List.class);
                    for (String imagePath : imagePaths) {
                        Files.deleteIfExists(Paths.get(imagePath));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            momentRepository.delete(moment);
            return true;
        }
        return false;
    }

    /**
     * 解析动态图片路径
     */
    public List<String> parseImagePaths(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(imagesJson, List.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 搜索动态
     */
    public List<UserMoment> searchMoments(String keyword) {
        return momentRepository.searchByContent(keyword);
    }

    /**
     * 获取动态详情
     */
    public Optional<UserMoment> getMomentById(Long momentId) {
        return findById(momentId);
    }
}
