package com.mzc.backend.lms.domains.notification.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 알림 수신 설정 엔티티 (사용자별)
 * notification_preferences 테이블과 매핑
 */
@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notification_preferences_user_id", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "idx_user_type_pref", columnNames = {"user_id", "type_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private NotificationType notificationType;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private NotificationPreference(User user, NotificationType notificationType,
                                   Boolean isEnabled, Boolean emailEnabled) {
        this.user = user;
        this.notificationType = notificationType;
        this.isEnabled = isEnabled != null ? isEnabled : true;
        this.emailEnabled = emailEnabled != null ? emailEnabled : false;
    }

    /**
     * 알림 수신 설정 생성 팩토리 메소드
     */
    public static NotificationPreference create(User user, NotificationType notificationType) {
        return NotificationPreference.builder()
                .user(user)
                .notificationType(notificationType)
                .isEnabled(true)
                .emailEnabled(false)
                .build();
    }

    /**
     * 알림 수신 설정 생성 팩토리 메소드 (옵션 포함)
     */
    public static NotificationPreference createWithOptions(User user, NotificationType notificationType,
                                                           boolean isEnabled, boolean emailEnabled) {
        return NotificationPreference.builder()
                .user(user)
                .notificationType(notificationType)
                .isEnabled(isEnabled)
                .emailEnabled(emailEnabled)
                .build();
    }

    /**
     * 알림 수신 활성화
     */
    public void enable() {
        this.isEnabled = true;
    }

    /**
     * 알림 수신 비활성화
     */
    public void disable() {
        this.isEnabled = false;
    }

    /**
     * 이메일 알림 활성화
     */
    public void enableEmail() {
        this.emailEnabled = true;
    }

    /**
     * 이메일 알림 비활성화
     */
    public void disableEmail() {
        this.emailEnabled = false;
    }

    /**
     * 알림 설정 업데이트
     */
    public void updatePreference(boolean isEnabled, boolean emailEnabled) {
        this.isEnabled = isEnabled;
        this.emailEnabled = emailEnabled;
    }
}