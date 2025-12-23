package com.mzc.backend.lms.domains.user.profile.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 이미지 엔티티 (1:1 관계)
 * user_profile_images 테이블과 매핑
 *
 * @MapsId 사용 시 JPA가 isNew()를 잘못 판단하는 문제 해결을 위해
 * Persistable 인터페이스 구현
 */
@Entity
@Table(name = "user_profile_images",
		indexes = {
				@Index(name = "idx_profile_image_created", columnList = "created_at")
		})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileImage implements Persistable<Long> {
	
	@Id
	@Column(name = "user_id")
	private Long userId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
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
	private UserProfileImage(User user, Long userId, String imageUrl, String thumbnailUrl) {
		if (user != null) {
			this.user = user;
			this.userId = user.getId();
		} else {
			this.userId = userId;
		}
		this.imageUrl = imageUrl;
		this.thumbnailUrl = thumbnailUrl;
	}
	
	/**
	 * 프로필 이미지 생성 (User 엔티티 사용)
	 */
	public static UserProfileImage create(User user, String imageUrl) {
		return UserProfileImage.builder()
				.user(user)
				.imageUrl(imageUrl)
				.build();
	}
	
	/**
	 * 프로필 이미지 생성 (userId만 사용 - cascade 문제 방지)
	 */
	public static UserProfileImage createWithUserId(Long userId, String imageUrl, String thumbnailUrl) {
		return UserProfileImage.builder()
				.userId(userId)
				.imageUrl(imageUrl)
				.thumbnailUrl(thumbnailUrl)
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
	
	/**
	 * Persistable 구현 - ID 반환
	 */
	@Override
	public Long getId() {
		return userId;
	}
	
	/**
	 * Persistable 구현 - 새 엔티티 여부 판단
	 * createdAt이 null이면 아직 persist 되지 않은 새 엔티티
	 */
	@Override
	public boolean isNew() {
		return createdAt == null;
	}
}
