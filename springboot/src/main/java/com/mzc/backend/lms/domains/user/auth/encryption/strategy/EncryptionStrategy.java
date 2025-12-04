package com.mzc.backend.lms.domains.user.auth.encryption.strategy;

/**
 * 암호화 전략 인터페이스
 * 데이터 타입별로 다른 암호화 알고리즘을 적용하기 위한 전략 패턴
 */
public interface EncryptionStrategy {

    /**
     * 데이터 암호화
     * @param plainText 평문 데이터
     * @return 암호화된 데이터
     */
    String encrypt(String plainText);

    /**
     * 데이터 복호화
     * @param encryptedText 암호화된 데이터
     * @return 복호화된 평문 데이터
     */
    String decrypt(String encryptedText);

    /**
     * 복호화 가능 여부
     * @return true: 양방향 암호화, false: 단방향 암호화
     */
    boolean isReversible();

    /**
     * 암호화 매칭 확인 (단방향 암호화용)
     * @param plainText 평문 데이터
     * @param encryptedText 암호화된 데이터
     * @return 매칭 여부
     */
    boolean matches(String plainText, String encryptedText);
}