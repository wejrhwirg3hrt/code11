package org.example.repository;

import org.example.entity.Conversation;
import org.example.entity.Message;
import org.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * 查找会话中的消息（分页）
     */
    Page<Message> findByConversationAndIsRecalledFalseOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);
    
    /**
     * 查找会话中的最新消息
     */
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.isRecalled = false AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Message> findLatestMessages(@Param("conversation") Conversation conversation, Pageable pageable);
    
    /**
     * 查找会话的最后一条消息
     */
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.isRecalled = false AND m.isDeleted = false ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessage(@Param("conversation") Conversation conversation);
    
    /**
     * 查找用户发送的消息
     */
    Page<Message> findBySenderAndIsRecalledFalseAndIsDeletedFalseOrderByCreatedAtDesc(User sender, Pageable pageable);
    
    /**
     * 查找特定时间范围内的消息
     */
    List<Message> findByConversationAndCreatedAtBetweenAndIsRecalledFalseAndIsDeletedFalseOrderByCreatedAtAsc(
        Conversation conversation, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    /**
     * 查找特定类型的消息
     */
    List<Message> findByConversationAndTypeAndIsRecalledFalseAndIsDeletedFalseOrderByCreatedAtDesc(
        Conversation conversation, 
        Message.MessageType type
    );
    
    /**
     * 统计会话中的消息数量
     */
    long countByConversationAndIsRecalledFalseAndIsDeletedFalse(Conversation conversation);
    
    /**
     * 查找包含特定内容的消息
     */
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.content LIKE %:keyword% AND m.isRecalled = false AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Message> searchMessages(@Param("conversation") Conversation conversation, @Param("keyword") String keyword);
    
    /**
     * 查找用户在会话中发送的消息数量
     */
    long countByConversationAndSenderAndIsRecalledFalseAndIsDeletedFalse(Conversation conversation, User sender);

    /**
     * 查找会话中未删除的消息
     */
    List<Message> findByConversationAndIsDeletedFalse(Conversation conversation);

    /**
     * 查找会话中的消息（包含撤回和删除状态过滤）
     */
    Page<Message> findByConversationAndIsRecalledFalseAndIsDeletedFalseOrderByCreatedAtDesc(
        Conversation conversation, Pageable pageable);

    /**
     * 根据会话ID查找最后一条消息（分页版本）
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isRecalled = false AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByConversationIdOrderByCreatedAtDesc(@Param("conversationId") Long conversationId, Pageable pageable);

    /**
     * 根据消息类型查找消息
     */
    List<Message> findByType(Message.MessageType type);
    
    /**
     * 删除用户发送的所有消息
     */
    void deleteBySenderId(Long userId);
}
