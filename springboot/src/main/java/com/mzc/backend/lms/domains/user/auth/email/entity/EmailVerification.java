package com.mzc.backend.lms.domains.user.auth.email.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 이메일 인증 엔티티
 * email_verifications 테이블과 매핑
 */
@Entity
@Table(name = "email_verifications", indexes = {
		@Index(name = "idx_email_verifications_email", columnList = "email"),
		@Index(name = "idx_email_verifications_code", columnList = "verification_code"),
		@Index(name = "idx_email_verifications_expires_at", columnList = "expires_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "email", length = 100, nullable = false)
	private String email;
	
	@Column(name = "verification_code", length = 5, nullable = false)
	private String verificationCode;
	
	@Column(name = "is_verified")
	private Boolean isVerified = false;
	
	@Column(name = "verification_attempts")
	private Integer verificationAttempts = 0;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;
	
	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;
	
	@Builder
	private EmailVerification(String email, String verificationCode, LocalDateTime expiresAt) {
		this.email = email;
		this.verificationCode = verificationCode;
		this.expiresAt = expiresAt;
		this.isVerified = false;
		this.verificationAttempts = 0;
	}
	
	/**
	 * 이메일 인증 생성
	 */
	public static EmailVerification create(String email, String verificationCode) {
		// 기본 5분 유효
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
		return EmailVerification.builder()
				.email(email)
				.verificationCode(verificationCode)
				.expiresAt(expiresAt)
				.build();
	}
	
	/**
	 * 인증 완료 처리
	 */
	public void verify() {
		this.isVerified = true;
		this.verifiedAt = LocalDateTime.now();
	}
	
	/**
	 * 인증 시도 횟수 증가
	 */
	public void increaseAttempts() {
		this.verificationAttempts++;
	}
	
	/**
	 * 만료 여부 확인
	 */
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiresAt);
	}
	
	/**
	 * 인증 가능 여부 확인
	 */
	public boolean canVerify() {
		return !isExpired() && !isVerified && verificationAttempts < 5;
	}
	
	/**
	 * 코드 매칭 확인
	 */
	public boolean matchesCode(String code) {
		return this.verificationCode.equals(code);
	}
}
