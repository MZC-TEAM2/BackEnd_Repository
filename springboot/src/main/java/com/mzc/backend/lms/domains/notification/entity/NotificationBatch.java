package com.mzc.backend.lms.domains.notification.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 대량 알림 발송 엔티티
 * notification_batches 테이블과 매핑
 */
@Entity
@Table(name = "notification_batches", indexes = {
    @Index(name = "idx_notification_batches_course_id", columnList = "course_id"),
    @Index(name = "idx_notification_batches_status", columnList = "status"),
    @Index(name = "idx_notification_batches_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private BatchStatus status = BatchStatus.PENDING;

    @Column(name = "total_recipients", nullable = false)
    private Integer totalRecipients;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Builder
    private NotificationBatch(NotificationType notificationType, User sender, Long courseId,
                              String title, String message, Integer totalRecipients) {
        this.notificationType = notificationType;
        this.sender = sender;
        this.courseId = courseId;
        this.title = title;
        this.message = message;
        this.totalRecipients = totalRecipients;
        this.status = BatchStatus.PENDING;
    }

    /**
     * 대량 알림 배치 생성 팩토리 메소드
     */
    public static NotificationBatch create(NotificationType notificationType, User sender,
                                           Long courseId, String title, String message,
                                           Integer totalRecipients) {
        return NotificationBatch.builder()
                .notificationType(notificationType)
                .sender(sender)
                .courseId(courseId)
                .title(title)
                .message(message)
                .totalRecipients(totalRecipients)
                .build();
    }

    /**
     * 배치 처리 시작
     */
    public void startProcessing() {
        this.status = BatchStatus.PROCESSING;
    }

    /**
     * 배치 처리 완료
     */
    public void complete() {
        this.status = BatchStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 배치 처리 실패
     */
    public void fail() {
        this.status = BatchStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 배치 처리 실패 (사유 포함)
     */
    public void fail(String reason) {
        this.status = BatchStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = reason != null && reason.length() > 500 ?
                reason.substring(0, 500) : reason;
    }

    /**
     * 처리 대기 상태인지 확인
     */
    public boolean isPending() {
        return this.status == BatchStatus.PENDING;
    }

    /**
     * 처리 중인지 확인
     */
    public boolean isProcessing() {
        return this.status == BatchStatus.PROCESSING;
    }

    /**
     * 완료되었는지 확인
     */
    public boolean isCompleted() {
        return this.status == BatchStatus.COMPLETED;
    }

    /**
     * 배치 상태 열거형
     */
    public enum BatchStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}