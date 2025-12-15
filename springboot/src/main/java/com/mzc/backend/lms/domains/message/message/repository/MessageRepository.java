package com.mzc.backend.lms.domains.message.message.repository;

import com.mzc.backend.lms.domains.message.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 메시지 Repository
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 대화방의 메시지 목록 조회 (최신순)
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation.id = :conversationId " +
           "ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdOrderByCreatedAtDesc(@Param("conversationId") Long conversationId);

    /**
     * 대화방의 안읽은 메시지 읽음 처리 (수신자 기준)
     */
    @Modifying
    @Query("UPDATE Message m SET m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id != :userId " +
           "AND m.readAt IS NULL")
    int markAllAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 양쪽 모두 삭제된 메시지 삭제
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.deletedBySender = true AND m.deletedByReceiver = true")
    int deleteAllDeletedByBoth();

    /**
     * 대화방의 양쪽 모두 삭제된 메시지 삭제
     */
    @Modifying
    @Query("DELETE FROM Message m " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.deletedBySender = true AND m.deletedByReceiver = true")
    int deleteDeletedByBothInConversation(@Param("conversationId") Long conversationId);
}
