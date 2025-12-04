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
 * 사용자 프로필 이미지 엔티티
 * user_profile_images 테이블과 매핑
 */
@Entity
@Table(name = "user_profile_images", indexes = {
    @Index(name = "idx_user_profile_images_user_id", columnList = "user_id"),
    @Index(name = "idx_user_profile_images_is_current", columnList = "is_current")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_current")
    private Boolean isCurrent = true;

    @CreationTimestamp
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Builder
    private UserProfileImage(User user, String imageUrl) {
        this.user = user;
        this.imageUrl = imageUrl;
        this.isCurrent = true;
    }

    /**
     * 프로필 이미지 생성
     */
    public static UserProfileImage create(User user, String imageUrl) {
        return UserProfileImage.builder()
                .user(user)
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * 현재 프로필 이미지로 설정
     */
    public void setCurrent() {
        this.isCurrent = true;
    }

    /**
     * 현재 프로필 이미지 해제
     */
    public void unsetCurrent() {
        this.isCurrent = false;
    }
}
