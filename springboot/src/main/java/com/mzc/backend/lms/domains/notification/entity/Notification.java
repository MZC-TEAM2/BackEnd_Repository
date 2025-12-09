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
 * 알림 메인 엔티티
 * notifications 테이블과 매핑
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_recipient_unread", columnList = "recipient_id, is_read, created_at"),
    @Index(name = "idx_recipient_created", columnList = "recipient_id, created_at"),
    @Index(name = "idx_course_created", columnList = "course_id, created_at"),
    @Index(name = "idx_notifications_type_id", columnList = "type_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Notification(NotificationType notificationType, User sender, User recipient,
                         String relatedEntityType, Long relatedEntityId, Long courseId,
                         String title, String message, String actionUrl) {
        this.notificationType = notificationType;
        this.sender = sender;
        this.recipient = recipient;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.courseId = courseId;
        this.title = title;
        this.message = message;
        this.actionUrl = actionUrl;
        this.isRead = false;
    }

    /**
     * 알림 생성 팩토리 메소드
     */
    public static Notification create(NotificationType notificationType, User sender,
                                       User recipient, String message) {
        return Notification.builder()
                .notificationType(notificationType)
                .sender(sender)
                .recipient(recipient)
                .message(message)
                .build();
    }

    /**
     * 관련 엔티티 정보가 포함된 알림 생성 팩토리 메소드
     */
    public static Notification createWithRelatedEntity(NotificationType notificationType,
                                                        User sender, User recipient,
                                                        String title, String message,
                                                        String relatedEntityType,
                                                        Long relatedEntityId,
                                                        Long courseId, String actionUrl) {
        return Notification.builder()
                .notificationType(notificationType)
                .sender(sender)
                .recipient(recipient)
                .title(title)
                .message(message)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .courseId(courseId)
                .actionUrl(actionUrl)
                .build();
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * 알림 읽지 않음 처리
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    /**
     * 읽음 여부 확인
     */
    public boolean isUnread() {
        return !this.isRead;
    }
}