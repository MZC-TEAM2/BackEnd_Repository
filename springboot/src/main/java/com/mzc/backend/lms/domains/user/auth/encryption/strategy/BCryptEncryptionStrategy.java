package com.mzc.backend.lms.domains.user.auth.encryption.strategy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt 암호화 전략 구현
 * 비밀번호 등 단방향 해싱이 필요한 데이터에 사용
 */
@Component
public class BCryptEncryptionStrategy implements EncryptionStrategy {
	
	private final BCryptPasswordEncoder encoder;
	
	public BCryptEncryptionStrategy() {
		// BCrypt strength 12 (참고 리포지토리 기준)
		this.encoder = new BCryptPasswordEncoder(12);
	}
	
	/**
	 * BCrypt 해싱
	 */
	@Override
	public String encrypt(String plainText) {
		if (plainText == null || plainText.isEmpty()) {
			return null;
		}
		return encoder.encode(plainText);
	}
	
	/**
	 * BCrypt는 단방향 해싱이므로 복호화 불가
	 */
	@Override
	public String decrypt(String encryptedText) {
		throw new UnsupportedOperationException("BCrypt는 단방향 해싱이므로 복호화를 지원하지 않습니다.");
	}
	
	/**
	 * 단방향 암호화
	 */
	@Override
	public boolean isReversible() {
		return false;
	}
	
	/**
	 * 비밀번호 매칭 확인
	 */
	@Override
	public boolean matches(String plainText, String encryptedText) {
		if (plainText == null || encryptedText == null) {
			return false;
		}
		return encoder.matches(plainText, encryptedText);
	}
}
