package org.example.repository;

import org.example.entity.Conversation;
import org.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    /**
     * 查找用户参与的所有会话
     */
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN c.participants p " +
           "WHERE p.user.id = :userId AND p.isActive = true AND c.isActive = true " +
           "ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findUserConversations(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 查找两个用户之间的私聊会话
     */
    @Query("SELECT c FROM Conversation c " +
           "WHERE c.type = 'PRIVATE' AND c.isActive = true " +
           "AND EXISTS (SELECT 1 FROM ConversationParticipant p1 WHERE p1.conversation = c AND p1.user.id = :user1Id AND p1.isActive = true) " +
           "AND EXISTS (SELECT 1 FROM ConversationParticipant p2 WHERE p2.conversation = c AND p2.user.id = :user2Id AND p2.isActive = true) " +
           "AND (SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation = c AND p.isActive = true) = 2")
    Optional<Conversation> findPrivateConversationBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    /**
     * 查找用户创建的会话
     */
    List<Conversation> findByCreatedByAndIsActiveTrue(User createdBy);
    
    /**
     * 查找活跃的会话
     */
    List<Conversation> findByIsActiveTrueOrderByLastMessageAtDesc();
    
    /**
     * 根据类型查找会话
     */
    List<Conversation> findByTypeAndIsActiveTrueOrderByLastMessageAtDesc(Conversation.ConversationType type);
}
