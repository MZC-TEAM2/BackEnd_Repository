package com.mzc.backend.lms.domains.user.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 상태 변경 이력 엔티티
 * user_status_history 테이블과 매핑
 */
@Entity
@Table(name = "user_status_history", indexes = {
    @Index(name = "idx_user_status_history_user_id", columnList = "user_id"),
    @Index(name = "idx_user_status_history_status_id", columnList = "status_id"),
    @Index(name = "idx_user_status_history_changed_at", columnList = "changed_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private UserStatusType status;

    @CreationTimestamp
    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;  // 변경 수행자 (관리자)

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Builder
    private UserStatusHistory(User user, UserStatusType status, User changedBy, String reason) {
        this.user = user;
        this.status = status;
        this.changedBy = changedBy;
        this.reason = reason;
    }

    /**
     * 상태 이력 생성
     */
    public static UserStatusHistory create(User user, UserStatusType status, User changedBy, String reason) {
        return UserStatusHistory.builder()
                .user(user)
                .status(status)
                .changedBy(changedBy)
                .reason(reason)
                .build();
    }
}