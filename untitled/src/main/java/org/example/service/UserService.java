package org.example.service;

import org.example.entity.User;
import org.example.entity.Comment;
import org.example.repository.UserRepository;
import org.example.util.AvatarUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AchievementService achievementService;

    // 获取总用户数
    public long getTotalUsers() {
        return userRepository.count();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 保存用户
    public User save(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // 注册用户时使用新方法名
    public User registerUser(String username, String email, String password) {
        return createUser(username, email, password);
    }

    // 根据ID查找用户（包括已删除的）
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User createUser(String username, String email, String password) {
        if (existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(true);
        user.setBanned(false);
        // 设置默认头像 - 使用AvatarUtil确保一致性
        user.setAvatar(AvatarUtil.DEFAULT_AVATAR_PATH);

        User savedUser = userRepository.save(user);

        // 触发注册成就检查
        try {
            achievementService.triggerAchievementCheck(savedUser, "REGISTER", 1);
        } catch (Exception e) {
            System.err.println("❌ 注册成就检查失败: " + e.getMessage());
        }

        return savedUser;
    }

    public void createDefaultAdmin() {
        if (!existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            admin.setBanned(false);
                    // 设置默认头像 - 使用AvatarUtil确保一致性
        admin.setAvatar(AvatarUtil.DEFAULT_AVATAR_PATH);
            userRepository.save(admin);
        }
    }

    public void resetPassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // 评论相关方法委托给CommentService
    public void deleteComment(Long commentId) {
        commentService.deleteComment(commentId);
    }

    public void likeComment(Long commentId) {
        commentService.likeComment(commentId);
    }

    public List<Comment> getCommentsByUserId(Long userId) {
        return commentService.getCommentsByUserId(userId);
    }

    public List<Comment> getReplies(Long parentId) {
        return commentService.getReplies(parentId);
    }

    public Comment createComment(Long videoId, Long userId, String username, String content, Long parentId) {
        return commentService.createComment(videoId, userId, username, content, parentId);
    }

    public Comment rejectComment(Long commentId, String reason) {
        commentService.rejectComment(commentId, reason);
        return commentService.getCommentById(commentId);
    }

    // 用户自主注销账号
    public void selfDeleteAccount(String username, String password, String reason) {
        Optional<User> userOpt = userRepository.findActiveByUsername(username);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在或已被注销");
        }

        User user = userOpt.get();

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        deleteAccount(user.getId(), reason);
    }

    // 验证用户密码
    public boolean checkPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    // 统计活跃用户数量
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    /**
     * 搜索用户
     */
    public Page<User> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.searchUsers(keyword, pageable);
    }

    /**
     * 搜索用户（简单版本）
     */
    public List<User> searchUsers(String keyword) {
        Pageable pageable = PageRequest.of(0, 20); // 限制返回20个结果
        return userRepository.searchUsers(keyword, pageable).getContent();
    }

    /**
     * 搜索用户（排除指定用户）
     */
    public List<User> searchUsers(String keyword, Long excludeUserId) {
        Pageable pageable = PageRequest.of(0, 20); // 限制返回20个结果
        return userRepository.searchUsersExcluding(keyword, excludeUserId, pageable);
    }

    /**
     * 获取所有活跃用户（排除指定用户）
     */
    public List<User> getAllActiveUsersExcluding(Long excludeUserId) {
        return userRepository.findAllActiveUsersExcluding(excludeUserId);
    }

    // 更新findByUsername方法，只查找活跃用户
    public Optional<User> findActiveByUsername(String username) {
        return userRepository.findActiveByUsername(username);
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 获取所有活跃用户
    public List<User> findAllActiveUsers() {
        return userRepository.findAllActiveUsers();
    }

    // 封禁用户（带管理员信息）
    public void banUser(Long userId, String reason, User admin) {
        User user = getUserById(userId);
        user.setBanned(true);
        user.setBanReason(reason);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 记录违规信息 - 暂时移除循环依赖，后续可以通过事件机制实现
        System.out.println("用户被封禁: ID=" + userId + ", 原因=" + reason + ", 管理员=" + (admin != null ? admin.getUsername() : "系统"));
    }

    // 获取所有已删除用户
    public List<User> getAllDeletedUsers() {
        return userRepository.findByDeletedTrue();
    }

    // 删除账号
    public void deleteAccount(Long userId, String reason) {
        User user = getUserById(userId);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeleteReason(reason);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 记录账号删除 - 暂时移除循环依赖，后续可以通过事件机制实现
        System.out.println("账号被删除: ID=" + userId + ", 原因=" + reason);
    }

    // 解封用户
    public void unbanUser(Long userId) {
        User user = getUserById(userId);
        user.setBanned(false);
        user.setBanReason(null);
        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // 检查用户是否存在
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    // 设置删除原因方法
    public void setDeleteReason(User user, String reason) {
        user.setDeleteReason(reason);
        userRepository.save(user);
    }

    // 获取所有活跃用户
    public List<User> getAllActiveUsers() {
        return userRepository.findByDeletedFalse();
    }

    // 根据用户名搜索用户（用于@功能）
    public List<User> searchUsersByUsername(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 使用现有的搜索方法，然后限制结果数量
        List<User> allResults = userRepository.findByUsernameContainingIgnoreCaseAndDeletedFalse(query.trim());

        // 限制结果数量
        if (allResults.size() > limit) {
            return allResults.subList(0, limit);
        }

        return allResults;
    }

    // 恢复账号方法
    public void restoreAccount(Long userId) {
        User user = getUserById(userId);
        if (!user.isDeleted()) {
            throw new RuntimeException("用户账号未被注销");
        }

        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeleteReason(null);
        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // 添加分页获取用户的方法
    public Page<User> getAllUsersPaged(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageRequest);
    }
}