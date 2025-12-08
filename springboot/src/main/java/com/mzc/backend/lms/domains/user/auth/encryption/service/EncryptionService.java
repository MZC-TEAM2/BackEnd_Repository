package com.mzc.backend.lms.domains.user.auth.encryption.service;

import com.mzc.backend.lms.domains.user.auth.encryption.strategy.AES256EncryptionStrategy;
import com.mzc.backend.lms.domains.user.auth.encryption.strategy.BCryptEncryptionStrategy;
import com.mzc.backend.lms.domains.user.auth.encryption.strategy.EncryptionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 암호화 서비스
 * 데이터 타입별로 적절한 암호화 전략을 선택하여 적용
 */
@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final AES256EncryptionStrategy aes256Strategy;
    private final BCryptEncryptionStrategy bcryptStrategy;

    /**
     * 이메일 암호화 (AES-256)
     */
    public String encryptEmail(String email) {
        return aes256Strategy.encrypt(email);
    }

    /**
     * 이메일 복호화 (AES-256)
     */
    public String decryptEmail(String encryptedEmail) {
        return aes256Strategy.decrypt(encryptedEmail);
    }

    /**
     * 전화번호 암호화 (AES-256)
     */
    public String encryptPhoneNumber(String phoneNumber) {
        return aes256Strategy.encrypt(phoneNumber);
    }

    /**
     * 전화번호 복호화 (AES-256)
     */
    public String decryptPhoneNumber(String encryptedPhoneNumber) {
        return aes256Strategy.decrypt(encryptedPhoneNumber);
    }

    /**
     * 이름 암호화 (AES-256)
     */
    public String encryptName(String name) {
        return aes256Strategy.encrypt(name);
    }

    /**
     * 이름 복호화 (AES-256)
     */
    public String decryptName(String encryptedName) {
        return aes256Strategy.decrypt(encryptedName);
    }

    /**
     * 비밀번호 암호화 (BCrypt)
     */
    public String encryptPassword(String password) {
        return bcryptStrategy.encrypt(password);
    }

    /**
     * 비밀번호 매칭 확인 (BCrypt)
     */
    public boolean matchesPassword(String plainPassword, String encryptedPassword) {
        return bcryptStrategy.matches(plainPassword, encryptedPassword);
    }

    /**
     * 개인정보 암호화 (AES-256)
     * 일반적인 개인정보 필드용
     */
    public String encryptPersonalInfo(String data) {
        return aes256Strategy.encrypt(data);
    }

    /**
     * 개인정보 복호화 (AES-256)
     */
    public String decryptPersonalInfo(String encryptedData) {
        return aes256Strategy.decrypt(encryptedData);
    }

    /**
     * 데이터 타입별 암호화 전략 선택
     */
    public EncryptionStrategy getStrategy(DataType dataType) {
        return switch (dataType) {
            case PASSWORD -> bcryptStrategy;
            case EMAIL, PHONE_NUMBER, PERSONAL_INFO -> aes256Strategy;
        };
    }

    /**
     * 데이터 타입 열거형
     */
    public enum DataType {
        PASSWORD,       // 비밀번호 (BCrypt)
        EMAIL,          // 이메일 (AES-256)
        PHONE_NUMBER,   // 전화번호 (AES-256)
        PERSONAL_INFO   // 기타 개인정보 (AES-256)
    }
}