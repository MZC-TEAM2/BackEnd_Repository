package com.mzc.backend.lms.domains.user.auth.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 이메일 인증 서비스
 * Redis를 활용한 인증 코드 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
	
	private static final String EMAIL_CODE_PREFIX = "email:code:";
	private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
	private static final String MASTER_CODE = "dding"; // 마스터 인증코드
	private static final int CODE_LENGTH = 5;
	private static final long CODE_TTL_MINUTES = 5; // 5분
	private static final long VERIFIED_TTL_MINUTES = 30; // 인증 완료 정보 30분 유지
	private final RedisTemplate<String, String> redisTemplate;
	private final EmailService emailService;
	@Value("${app.email.verification.enabled:true}")
	private boolean emailVerificationEnabled;
	
	/**
	 * 인증 코드 발송
	 */
	public void sendVerificationCode(String email) {
		// 이메일 중복 확인은 별도 서비스에서 처리
		String verificationCode = generateVerificationCode();
		
		// Redis에 저장 (5분 TTL)
		String key = EMAIL_CODE_PREFIX + email;
		redisTemplate.opsForValue().set(key, verificationCode, CODE_TTL_MINUTES, TimeUnit.MINUTES);
		
		// 이메일 발송
		if (emailVerificationEnabled) {
			emailService.sendVerificationCode(email, verificationCode);
		}
		
		log.info("인증 코드 발송: email={}, code={}", email, verificationCode);
	}
	
	/**
	 * 인증 코드 검증
	 */
	public boolean verifyCode(String email, String code) {
		// 마스터 코드 체크
		if (MASTER_CODE.equals(code)) {
			markAsVerified(email);
			log.info("마스터 코드로 인증 완료: email={}", email);
			return true;
		}
		
		String key = EMAIL_CODE_PREFIX + email;
		String storedCode = redisTemplate.opsForValue().get(key);
		
		if (storedCode == null) {
			log.warn("인증 코드 없음 또는 만료: email={}", email);
			return false;
		}
		
		if (storedCode.equals(code)) {
			// 인증 성공 - 코드 삭제하고 인증 완료 표시
			redisTemplate.delete(key);
			markAsVerified(email);
			log.info("인증 성공: email={}", email);
			return true;
		}
		
		log.warn("인증 코드 불일치: email={}, provided={}, expected={}", email, code, storedCode);
		return false;
	}
	
	/**
	 * 이메일 인증 완료 여부 확인
	 */
	public boolean isEmailVerified(String email) {
		String key = EMAIL_VERIFIED_PREFIX + email;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
	
	/**
	 * 이메일 인증 완료 표시
	 */
	private void markAsVerified(String email) {
		String key = EMAIL_VERIFIED_PREFIX + email;
		redisTemplate.opsForValue().set(key, "true", VERIFIED_TTL_MINUTES, TimeUnit.MINUTES);
	}
	
	/**
	 * 5자리 랜덤 인증 코드 생성
	 */
	private String generateVerificationCode() {
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder(CODE_LENGTH);
		
		// 숫자와 영문자 조합
		String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		for (int i = 0; i < CODE_LENGTH; i++) {
			int index = random.nextInt(characters.length());
			code.append(characters.charAt(index));
		}
		
		return code.toString();
	}
	
	/**
	 * 인증 정보 초기화 (테스트용)
	 */
	public void clearVerification(String email) {
		redisTemplate.delete(EMAIL_CODE_PREFIX + email);
		redisTemplate.delete(EMAIL_VERIFIED_PREFIX + email);
	}
}
