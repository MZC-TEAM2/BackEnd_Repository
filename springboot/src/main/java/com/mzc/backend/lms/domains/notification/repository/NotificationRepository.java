package com.mzc.backend.lms.domains.notification.repository;

import com.mzc.backend.lms.domains.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 알림 Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 수신자 ID로 알림 목록 조회 (최신순)
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    /**
     * 수신자 ID와 읽음 상태로 알림 목록 조회
     */
    Page<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(
            Long recipientId, Boolean isRead, Pageable pageable);

    /**
     * 수신자의 읽지 않은 알림 목록 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 수신자의 읽지 않은 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false")
    long countUnreadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 강의 ID로 알림 목록 조회
     */
    Page<Notification> findByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);

    /**
     * 관련 엔티티로 알림 목록 조회
     */
    List<Notification> findByRelatedEntityTypeAndRelatedEntityId(
            String relatedEntityType, Long relatedEntityId);

    /**
     * 수신자의 모든 알림 읽음 처리 (벌크 업데이트)
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 특정 알림 타입의 알림 목록 조회
     */
    Page<Notification> findByRecipientIdAndNotificationTypeIdOrderByCreatedAtDesc(
            Long recipientId, Integer typeId, Pageable pageable);

    // ========== 커서 기반 페이징 ==========

    /**
     * 커서 기반 알림 목록 조회 (최초 요청)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.id DESC")
    List<Notification> findByRecipientIdOrderByIdDesc(
            @Param("recipientId") Long recipientId, Pageable pageable);

    /**
     * 커서 기반 알림 목록 조회 (다음 페이지)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.id < :cursor ORDER BY n.id DESC")
    List<Notification> findByRecipientIdAndIdLessThanOrderByIdDesc(
            @Param("recipientId") Long recipientId,
            @Param("cursor") Long cursor,
            Pageable pageable);

    /**
     * 커서 기반 읽지 않은 알림 목록 조회 (최초 요청)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false ORDER BY n.id DESC")
    List<Notification> findUnreadByRecipientIdOrderByIdDesc(
            @Param("recipientId") Long recipientId, Pageable pageable);

    /**
     * 커서 기반 읽지 않은 알림 목록 조회 (다음 페이지)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false AND n.id < :cursor ORDER BY n.id DESC")
    List<Notification> findUnreadByRecipientIdAndIdLessThanOrderByIdDesc(
            @Param("recipientId") Long recipientId,
            @Param("cursor") Long cursor,
            Pageable pageable);

    // ========== 벌크 삭제 ==========

    /**
     * 읽은 알림 일괄 삭제 (벌크 삭제)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = true")
    int deleteReadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 모든 알림 삭제 (벌크 삭제)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :recipientId")
    int deleteAllByRecipientId(@Param("recipientId") Long recipientId);
}
