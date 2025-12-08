package com.mzc.backend.lms.domains.user.profile.entity;

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
 * 사용자 프로필 이미지 엔티티 (1:1 관계)
 * user_profile_images 테이블과 매핑
 */
@Entity
@Table(name = "user_profile_images",
    indexes = {
        @Index(name = "idx_profile_image_created", columnList = "created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileImage {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private UserProfileImage(User user, String imageUrl, String thumbnailUrl) {
        this.user = user;
        this.userId = user.getId();
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
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
     * 프로필 이미지 업데이트
     */
    public void updateImage(String imageUrl, String thumbnailUrl) {
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * 썸네일 URL 설정
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
