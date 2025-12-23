package com.mzc.backend.lms.domains.user.user.entity;

import com.mzc.backend.lms.domains.user.profile.entity.UserPrimaryContact;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * 사용자 기본 정보 엔티티
 * users 테이블과 매핑
 */
@Entity
@Table(name = "users", indexes = {
		@Index(name = "idx_users_email", columnList = "email"),
		@Index(name = "idx_users_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")  // Soft Delete
public class User implements Persistable<Long> {
	
	@Id
	@Column(name = "id")
	private Long id;  // 학번 또는 교번
	
	@Column(name = "email", length = 100, unique = true, nullable = false)
	private String email;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	
	// 1:1 연관관계 - 양방향 매핑
	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private UserProfile userProfile;
	
	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private UserPrimaryContact primaryContact;
	
	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private UserProfileImage profileImage;
	
	@Transient
	private boolean isNew = true;
	
	@Builder
	private User(Long id, String email, String password) {
		this.id = id;
		this.email = email;
		this.password = password;
	}
	
	/**
	 * 사용자 생성 팩토리 메소드
	 */
	public static User create(Long id, String email, String password) {
		return User.builder()
				.id(id)
				.email(email)
				.password(password)
				.build();
	}
	
	@Override
	public boolean isNew() {
		return isNew;
	}
	
	@PostPersist
	@PostLoad
	void markNotNew() {
		this.isNew = false;
	}
	
	/**
	 * 비밀번호 변경
	 */
	public void changePassword(String newPassword) {
		this.password = newPassword;
	}
	
	/**
	 * 소프트 삭제
	 */
	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}
	
	/**
	 * 복구
	 */
	public void restore() {
		this.deletedAt = null;
	}
	
	/**
	 * 삭제 여부 확인
	 */
	public boolean isDeleted() {
		return this.deletedAt != null;
	}
}
