package org.example.repository;

import org.example.entity.Conversation;
import org.example.entity.ConversationParticipant;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    
    /**
     * 查找会话的所有活跃参与者
     */
    List<ConversationParticipant> findByConversationAndIsActiveTrueOrderByJoinedAtAsc(Conversation conversation);
    
    /**
     * 查找用户在特定会话中的参与记录
     */
    Optional<ConversationParticipant> findByConversationAndUserAndIsActiveTrue(Conversation conversation, User user);
    
    /**
     * 查找用户参与的所有会话
     */
    List<ConversationParticipant> findByUserAndIsActiveTrueOrderByJoinedAtDesc(User user);
    
    /**
     * 检查用户是否是会话参与者
     */
    boolean existsByConversationAndUserAndIsActiveTrue(Conversation conversation, User user);

    /**
     * 检查用户是否是会话参与者（不考虑活跃状态）
     */
    boolean existsByConversationAndUser(Conversation conversation, User user);
    
    /**
     * 统计会话的活跃参与者数量
     */
    @Query("SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation = :conversation AND p.isActive = true")
    long countActiveParticipants(@Param("conversation") Conversation conversation);
    
    /**
     * 查找会话中的管理员
     */
    List<ConversationParticipant> findByConversationAndRoleInAndIsActiveTrue(
        Conversation conversation,
        List<ConversationParticipant.ParticipantRole> roles
    );

    /**
     * 查找会话的所有参与者（包括非活跃的）
     */
    List<ConversationParticipant> findByConversation(Conversation conversation);
    
    /**
     * 删除用户的所有会话参与记录
     */
    void deleteByUserId(Long userId);
}
