package com.mzc.backend.lms.domains.user.auth.email.repository;

import com.mzc.backend.lms.domains.user.auth.email.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 이메일 인증 레포지토리
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
	
	/**
	 * 이메일과 인증 코드로 조회
	 */
	Optional<EmailVerification> findByEmailAndVerificationCode(String email, String verificationCode);
	
	/**
	 * 가장 최근 이메일 인증 조회
	 */
	Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
	
	/**
	 * 이메일로 미인증 상태 조회
	 */
	@Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.isVerified = false AND ev.expiresAt > :now")
	Optional<EmailVerification> findValidByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
	
	/**
	 * 이메일 인증 완료 여부 확인
	 */
	boolean existsByEmailAndIsVerifiedTrue(String email);
	
	/**
	 * 만료된 인증 정보 삭제
	 */
	@Modifying
	@Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
	void deleteExpiredVerifications(@Param("now") LocalDateTime now);
}
