package com.mzc.backend.lms.domains.user.profile.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 엔티티
 * user_profiles 테이블과 매핑
 */
@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @Column(name = "user_id", length = 20)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    private UserProfile(User user, String name) {
        this.user = user;
        this.name = name;
        this.userId = user.getId();
    }

    /**
     * 프로필 생성
     */
    public static UserProfile create(User user, String name) {
        return UserProfile.builder()
                .user(user)
                .name(name)
                .build();
    }

    /**
     * 이름 변경
     */
    public void changeName(String newName) {
        this.name = newName;
    }
}
