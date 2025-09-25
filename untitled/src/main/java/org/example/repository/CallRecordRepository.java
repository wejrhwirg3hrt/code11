package org.example.repository;

import org.example.entity.CallRecord;
import org.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 通话记录Repository
 */
@Repository
public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {
    
    /**
     * 根据房间ID查找通话记录
     */
    Optional<CallRecord> findByRoomId(String roomId);
    
    /**
     * 查找用户的通话记录（作为主叫或被叫）
     */
    @Query("SELECT cr FROM CallRecord cr WHERE cr.caller = :user OR cr.callee = :user ORDER BY cr.createdAt DESC")
    Page<CallRecord> findByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * 查找两个用户之间的通话记录
     */
    @Query("SELECT cr FROM CallRecord cr WHERE " +
           "(cr.caller = :user1 AND cr.callee = :user2) OR " +
           "(cr.caller = :user2 AND cr.callee = :user1) " +
           "ORDER BY cr.createdAt DESC")
    Page<CallRecord> findByTwoUsers(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);
    
    /**
     * 查找用户的未接听通话
     */
    @Query("SELECT cr FROM CallRecord cr WHERE cr.callee = :user AND cr.callStatus = 'MISSED' ORDER BY cr.createdAt DESC")
    List<CallRecord> findMissedCallsByUser(@Param("user") User user);
    
    /**
     * 统计用户的通话次数
     */
    @Query("SELECT COUNT(cr) FROM CallRecord cr WHERE (cr.caller = :user OR cr.callee = :user) AND cr.callStatus = 'ENDED'")
    long countCompletedCallsByUser(@Param("user") User user);
    
    /**
     * 统计用户的通话总时长
     */
    @Query("SELECT COALESCE(SUM(cr.duration), 0) FROM CallRecord cr WHERE (cr.caller = :user OR cr.callee = :user) AND cr.callStatus = 'ENDED'")
    long sumCallDurationByUser(@Param("user") User user);
    
    /**
     * 查找指定时间范围内的通话记录
     */
    @Query("SELECT cr FROM CallRecord cr WHERE " +
           "(cr.caller = :user OR cr.callee = :user) AND " +
           "cr.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY cr.createdAt DESC")
    List<CallRecord> findByUserAndTimeRange(@Param("user") User user, 
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找指定状态的通话记录
     */
    @Query("SELECT cr FROM CallRecord cr WHERE " +
           "(cr.caller = :user OR cr.callee = :user) AND " +
           "cr.callStatus = :status " +
           "ORDER BY cr.createdAt DESC")
    List<CallRecord> findByUserAndStatus(@Param("user") User user, @Param("status") CallRecord.CallStatus status);
    
    /**
     * 查找指定类型的通话记录
     */
    @Query("SELECT cr FROM CallRecord cr WHERE " +
           "(cr.caller = :user OR cr.callee = :user) AND " +
           "cr.callType = :callType " +
           "ORDER BY cr.createdAt DESC")
    List<CallRecord> findByUserAndCallType(@Param("user") User user, @Param("callType") CallRecord.CallType callType);
    
    /**
     * 删除指定时间之前的通话记录
     */
    @Query("DELETE FROM CallRecord cr WHERE cr.createdAt < :cutoffTime")
    void deleteOldRecords(@Param("cutoffTime") LocalDateTime cutoffTime);
}
