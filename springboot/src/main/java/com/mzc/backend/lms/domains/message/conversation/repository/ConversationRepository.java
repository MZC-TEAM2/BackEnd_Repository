package com.mzc.backend.lms.domains.message.conversation.repository;

import com.mzc.backend.lms.domains.message.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 대화방 Repository
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * 두 사용자 간 대화방 조회 (user1_id < user2_id 순서로 저장됨)
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.user1.id = :smallerId AND c.user2.id = :largerId)")
    Optional<Conversation> findByUsers(@Param("smallerId") Long smallerId, @Param("largerId") Long largerId);

    /**
     * 두 사용자 간 대화방 조회 (순서 무관)
     */
    default Optional<Conversation> findByTwoUsers(Long userId1, Long userId2) {
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        return findByUsers(smallerId, largerId);
    }

    /**
     * 사용자의 대화방 목록 조회 (삭제되지 않은 것만, 최근 메시지 순)
     */
    @Query("SELECT c FROM Conversation c " +
           "WHERE (c.user1.id = :userId AND c.user1DeletedAt IS NULL) " +
           "   OR (c.user2.id = :userId AND c.user2DeletedAt IS NULL) " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findByUserIdOrderByLastMessageAtDesc(@Param("userId") Long userId);

    /**
     * 사용자의 전체 안읽음 수 조회
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN c.user1.id = :userId THEN c.user1UnreadCount " +
           "                        WHEN c.user2.id = :userId THEN c.user2UnreadCount " +
           "                        ELSE 0 END), 0) " +
           "FROM Conversation c " +
           "WHERE (c.user1.id = :userId AND c.user1DeletedAt IS NULL) " +
           "   OR (c.user2.id = :userId AND c.user2DeletedAt IS NULL)")
    int getTotalUnreadCount(@Param("userId") Long userId);
}
