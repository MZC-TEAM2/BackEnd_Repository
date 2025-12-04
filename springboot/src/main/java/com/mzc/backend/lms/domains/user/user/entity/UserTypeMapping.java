package com.mzc.backend.lms.domains.user.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자-유형 매핑 엔티티
 * user_type_mappings 테이블과 매핑
 */
@Entity
@Table(name = "user_type_mappings", indexes = {
    @Index(name = "idx_user_type_mappings_user_type_id", columnList = "user_type_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTypeMapping {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_type_id", nullable = false)
    private UserType userType;

    @CreationTimestamp
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Builder
    private UserTypeMapping(User user, UserType userType) {
        this.user = user;
        this.userType = userType;
        this.userId = user.getId();
    }

    /**
     * 사용자 유형 매핑 생성
     */
    public static UserTypeMapping create(User user, UserType userType) {
        return UserTypeMapping.builder()
                .user(user)
                .userType(userType)
                .build();
    }

    /**
     * 사용자 유형 변경
     */
    public void changeUserType(UserType newUserType) {
        this.userType = newUserType;
    }
}