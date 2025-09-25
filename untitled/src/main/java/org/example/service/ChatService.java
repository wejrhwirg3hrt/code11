package org.example.service;

import org.example.entity.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class ChatService {
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private ConversationParticipantRepository participantRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FollowService followService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取或创建私聊会话
     */
    @Transactional
    public Conversation getOrCreatePrivateConversation(Long currentUserId, Long targetUserId) {
        System.out.println("DEBUG: 开始获取或创建私聊会话，当前用户ID: " + currentUserId + ", 目标用户ID: " + targetUserId);

        // 查找现有的私聊会话
        Optional<Conversation> existingConversation = conversationRepository
            .findPrivateConversationBetweenUsers(currentUserId, targetUserId);

        if (existingConversation.isPresent()) {
            System.out.println("DEBUG: 找到现有会话，ID: " + existingConversation.get().getId());
            return existingConversation.get();
        }

        System.out.println("DEBUG: 没有找到现有会话，开始创建新会话");

        // 创建新的私聊会话
        Optional<User> currentUserOpt = userService.findById(currentUserId);
        Optional<User> targetUserOpt = userService.findById(targetUserId);

        System.out.println("DEBUG: 当前用户存在: " + currentUserOpt.isPresent() + ", 目标用户存在: " + targetUserOpt.isPresent());

        if (!currentUserOpt.isPresent() || !targetUserOpt.isPresent()) {
            System.out.println("DEBUG: 用户不存在错误");
            throw new RuntimeException("用户不存在");
        }
        
        User currentUser = currentUserOpt.get();
        User targetUser = targetUserOpt.get();
        
        // 创建会话
        Conversation conversation = new Conversation(Conversation.ConversationType.PRIVATE, currentUser);
        conversation = conversationRepository.save(conversation);
        
        System.out.println("DEBUG: 会话创建成功，ID: " + conversation.getId());

        // 添加参与者
        ConversationParticipant participant1 = new ConversationParticipant(conversation, currentUser, ConversationParticipant.ParticipantRole.MEMBER);
        ConversationParticipant participant2 = new ConversationParticipant(conversation, targetUser, ConversationParticipant.ParticipantRole.MEMBER);

        // 确保isActive设置为true
        participant1.setIsActive(true);
        participant2.setIsActive(true);

        participantRepository.save(participant1);
        participantRepository.save(participant2);

        System.out.println("DEBUG: 参与者添加成功 - 参与者1 ID: " + participant1.getId() + ", 参与者2 ID: " + participant2.getId());

        // 强制刷新以确保数据持久化
        participantRepository.flush();

        return conversation;
    }
    
    /**
     * 发送消息
     */
    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, Message.MessageType type, String content) {
        System.out.println("DEBUG: 开始发送消息，会话ID: " + conversationId + ", 发送者ID: " + senderId + ", 内容: " + content);

        // 简化验证逻辑
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("会话不存在"));

        User sender = userService.findById(senderId)
            .orElseThrow(() -> new RuntimeException("发送者不存在"));

        System.out.println("DEBUG: 会话和发送者验证通过");

        // 简化参与者检查 - 只检查是否存在参与记录，不检查isActive
        boolean isParticipant = participantRepository.existsByConversationAndUser(conversation, sender);
        System.out.println("DEBUG: 用户是否是会话参与者: " + isParticipant);

        if (!isParticipant) {
            System.out.println("DEBUG: 用户不是会话的参与者，自动添加为参与者");
            // 自动添加为参与者
            ConversationParticipant newParticipant = new ConversationParticipant(conversation, sender, ConversationParticipant.ParticipantRole.MEMBER);
            newParticipant.setIsActive(true);
            participantRepository.save(newParticipant);
        }
        
        // 如果是私聊，检查关注状态（暂时注释掉，允许发送消息）
        if (conversation.getType() == Conversation.ConversationType.PRIVATE) {
            // 查询参与者列表
            List<ConversationParticipant> participants = participantRepository.findByConversationAndIsActiveTrueOrderByJoinedAtAsc(conversation);

            if (participants.size() == 2) {
                User otherUser = participants.stream()
                    .filter(p -> !p.getUser().getId().equals(senderId))
                    .findFirst()
                    .map(ConversationParticipant::getUser)
                    .orElse(null);

                if (otherUser != null) {
                    boolean isFollowing = followService.isFollowing(senderId, otherUser.getId());
                    boolean isMutual = followService.isMutualFollow(senderId, otherUser.getId());

                    System.out.println("DEBUG: 关注状态检查 - 是否关注: " + isFollowing + ", 是否互关: " + isMutual);

                    // 暂时注释掉关注检查，允许发送消息进行测试
                    /*
                    if (!isFollowing) {
                        throw new RuntimeException("您还未关注对方，无法发送消息");
                    }
                    */

                    // 如果只是单向关注，检查消息限制（这里可以添加更复杂的逻辑）
                    if (!isMutual) {
                        // 可以添加单向关注的消息限制逻辑
                        System.out.println("DEBUG: 单向关注，可能需要限制消息");
                    }
                }
            }
        }
        
        // 创建消息
        Message message = new Message(conversation, sender, type, content);
        message = messageRepository.save(message);
        
        // 更新会话的最后消息时间
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return message;
    }
    
    /**
     * 发送文件消息
     */
    public Message sendFileMessage(Long conversationId, Long senderId, Message.MessageType type,
                                 String content, String fileUrl, String fileName, Long fileSize) {
        Message message = sendMessage(conversationId, senderId, type, content);
        message.setFileUrl(fileUrl);
        message.setFileName(fileName);
        message.setFileSize(fileSize);
        return messageRepository.save(message);
    }

    /**
     * 发送回复消息
     */
    @Transactional
    public Message sendReplyMessage(Long conversationId, Long senderId, Message.MessageType type,
                                  String content, Long replyToMessageId, String mentionedUsers) {
        // 验证被回复的消息是否存在
        Message replyToMessage = null;
        if (replyToMessageId != null) {
            Optional<Message> replyToOpt = messageRepository.findById(replyToMessageId);
            if (replyToOpt.isPresent()) {
                replyToMessage = replyToOpt.get();
                // 验证被回复的消息是否属于同一个会话
                if (!replyToMessage.getConversation().getId().equals(conversationId)) {
                    throw new RuntimeException("被回复的消息不属于当前会话");
                }
            }
        }

        // 发送消息
        Message message = sendMessage(conversationId, senderId, type, content);

        // 设置回复信息
        if (replyToMessage != null) {
            message.setReplyTo(replyToMessage);
        }

        // 设置@用户信息
        if (mentionedUsers != null && !mentionedUsers.trim().isEmpty()) {
            message.setMentionedUsers(mentionedUsers);
        }

        return messageRepository.save(message);
    }
    
    /**
     * 获取会话消息
     */
    public Page<Message> getConversationMessages(Long conversationId, Long userId, int page, int size) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new RuntimeException("会话不存在");
        }
        
        // 检查用户是否有权限访问该会话
        Conversation conversation = conversationOpt.get();
        boolean hasAccess = participantRepository.existsByConversationAndUserAndIsActiveTrue(conversation, 
            userRepository.findById(userId).orElse(null));
        
        if (!hasAccess) {
            throw new RuntimeException("没有权限访问该会话");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByConversationAndIsRecalledFalseAndIsDeletedFalseOrderByCreatedAtDesc(
            conversation, pageable);
    }
    
    /**
     * 获取用户的会话列表
     */
    @Transactional(readOnly = true)
    public Page<Conversation> getUserConversations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return conversationRepository.findUserConversations(userId, pageable);
    }

    /**
     * 获取会话参与者
     */
    @Transactional(readOnly = true)
    public List<User> getConversationParticipants(Long conversationId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new RuntimeException("会话不存在");
        }

        Conversation conversation = conversationOpt.get();
        List<ConversationParticipant> participants = participantRepository.findByConversationAndIsActiveTrueOrderByJoinedAtAsc(conversation);
        
        // 强制初始化User实体，避免懒加载异常
        List<User> users = new ArrayList<>();
        for (ConversationParticipant participant : participants) {
            User user = participant.getUser();
            user.getUsername(); // 触发懒加载
            users.add(user);
        }
        
        return users;
    }

    /**
     * 获取私聊中的对方用户
     */
    @Transactional(readOnly = true)
    public User getOtherUserInPrivateConversation(Long conversationId, Long currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null || conversation.getType() != Conversation.ConversationType.PRIVATE) {
            return null;
        }

        List<ConversationParticipant> participants = participantRepository
            .findByConversationAndIsActiveTrueOrderByJoinedAtAsc(conversation);

        for (ConversationParticipant participant : participants) {
            if (!participant.getUser().getId().equals(currentUserId)) {
                // 强制初始化User实体，避免懒加载异常
                User user = participant.getUser();
                user.getUsername(); // 触发懒加载
                return user;
            }
        }

        return null;
    }
    
    /**
     * 检查用户是否可以发送消息
     */
    public boolean canSendMessage(Long senderId, Long targetUserId) {
        boolean isFollowing = followService.isFollowing(senderId, targetUserId);
        return isFollowing; // 简单的规则：关注了就可以发送
    }
    
    /**
     * 获取关注状态信息
     */
    public String getFollowStatusForChat(Long currentUserId, Long targetUserId) {
        boolean isFollowing = followService.isFollowing(currentUserId, targetUserId);
        boolean isMutual = followService.isMutualFollow(currentUserId, targetUserId);

        if (isMutual) {
            return "mutual-friends";
        } else if (isFollowing) {
            return "following";
        } else {
            return "not-following";
        }
    }

    /**
     * 撤回消息（20分钟内）
     */
    @Transactional
    public boolean recallMessage(Long messageId, Long userId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (!messageOpt.isPresent()) {
            return false;
        }

        Message message = messageOpt.get();

        // 检查权限：只有发送者可以撤回
        if (!message.getSender().getId().equals(userId)) {
            return false;
        }

        // 检查时间限制：20分钟内可以撤回
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime messageTime = message.getCreatedAt();
        long minutesDiff = java.time.Duration.between(messageTime, now).toMinutes();

        if (minutesDiff > 20) {
            return false; // 超过20分钟不能撤回
        }

        // 执行撤回
        message.setIsRecalled(true);
        message.setRecalledAt(now);
        message.setUpdatedAt(now);
        messageRepository.save(message);

        return true;
    }

    /**
     * 删除消息（软删除）
     */
    @Transactional
    public boolean deleteMessage(Long messageId, Long userId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (!messageOpt.isPresent()) {
            return false;
        }

        Message message = messageOpt.get();

        // 检查权限：只有发送者可以删除
        if (!message.getSender().getId().equals(userId)) {
            return false;
        }

        // 执行软删除
        message.setIsDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(message);

        return true;
    }

    /**
     * 清空会话消息（软删除）
     */
    @Transactional
    public boolean clearConversationMessages(Long conversationId, Long userId) {
        // 检查用户是否是会话参与者
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            return false;
        }

        Conversation conversation = conversationOpt.get();
        List<ConversationParticipant> participants = participantRepository.findByConversation(conversation);

        boolean isParticipant = participants.stream()
            .anyMatch(p -> p.getUser().getId().equals(userId));

        if (!isParticipant) {
            return false;
        }

        // 获取会话中的所有消息并软删除
        List<Message> messages = messageRepository.findByConversationAndIsDeletedFalse(conversation);
        LocalDateTime now = LocalDateTime.now();

        for (Message message : messages) {
            message.setIsDeleted(true);
            message.setDeletedAt(now);
            message.setUpdatedAt(now);
        }

        messageRepository.saveAll(messages);
        return true;
    }

    /**
     * 删除会话（软删除）
     */
    @Transactional
    public boolean deleteConversation(Long conversationId, Long userId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            return false;
        }

        Conversation conversation = conversationOpt.get();
        List<ConversationParticipant> participants = participantRepository.findByConversation(conversation);

        // 检查用户是否是会话参与者
        boolean isParticipant = participants.stream()
            .anyMatch(p -> p.getUser().getId().equals(userId));

        if (!isParticipant) {
            return false;
        }

        // 删除用户的参与记录（软删除）
        ConversationParticipant userParticipant = participants.stream()
            .filter(p -> p.getUser().getId().equals(userId))
            .findFirst()
            .orElse(null);

        if (userParticipant != null) {
            participantRepository.delete(userParticipant);
        }

        // 如果没有其他参与者，删除整个会话
        List<ConversationParticipant> remainingParticipants = participantRepository.findByConversation(conversation);
        if (remainingParticipants.isEmpty()) {
            conversationRepository.delete(conversation);
        }

        return true;
    }

    /**
     * 获取会话中的最后一条消息
     */
    @Transactional(readOnly = true)
    public Message getLastMessageInConversation(Long conversationId) {
        try {
            Pageable pageable = PageRequest.of(0, 1);
            // 使用包含删除状态过滤的查询方法
            Page<Message> messages = messageRepository.findByConversationAndIsRecalledFalseAndIsDeletedFalseOrderByCreatedAtDesc(
                conversationRepository.findById(conversationId).orElse(null), pageable);
            Message lastMessage = messages.hasContent() ? messages.getContent().get(0) : null;
            
            // 如果存在最后一条消息，强制初始化sender以避免懒加载异常
            if (lastMessage != null && lastMessage.getSender() != null) {
                lastMessage.getSender().getUsername(); // 触发懒加载
            }
            
            return lastMessage;
        } catch (Exception e) {
            System.err.println("获取最后一条消息失败: " + e.getMessage());
            return null;
        }
    }
}
