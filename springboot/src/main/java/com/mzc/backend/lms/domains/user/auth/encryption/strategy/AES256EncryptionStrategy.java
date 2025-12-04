package com.mzc.backend.lms.domains.user.auth.encryption.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-256 암호화 전략 구현
 * 이메일, 전화번호 등 복호화가 필요한 개인정보에 사용
 */
@Component
public class AES256EncryptionStrategy implements EncryptionStrategy {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES = "AES";

    @Value("${encryption.aes.secret-key:defaultSecretKey1234567890123456}")
    private String secretKey;

    @Value("${encryption.aes.iv:defaultIvParam456}")
    private String iv;

    /**
     * AES-256 암호화
     */
    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(getKey(), AES);
            IvParameterSpec ivSpec = new IvParameterSpec(getIv());

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES256 암호화 실패", e);
        }
    }

    /**
     * AES-256 복호화
     */
    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(getKey(), AES);
            IvParameterSpec ivSpec = new IvParameterSpec(getIv());

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decodedBytes);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES256 복호화 실패", e);
        }
    }

    /**
     * 양방향 암호화 지원
     */
    @Override
    public boolean isReversible() {
        return true;
    }

    /**
     * 암호화 매칭 확인
     */
    @Override
    public boolean matches(String plainText, String encryptedText) {
        if (plainText == null || encryptedText == null) {
            return false;
        }

        try {
            String decrypted = decrypt(encryptedText);
            return plainText.equals(decrypted);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 32바이트 키 생성 (AES-256)
     */
    private byte[] getKey() {
        byte[] key = new byte[32];
        byte[] paramBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        int len = Math.min(paramBytes.length, key.length);
        System.arraycopy(paramBytes, 0, key, 0, len);

        return key;
    }

    /**
     * 16바이트 IV 생성
     */
    private byte[] getIv() {
        byte[] ivBytes = new byte[16];
        byte[] paramBytes = iv.getBytes(StandardCharsets.UTF_8);

        int len = Math.min(paramBytes.length, ivBytes.length);
        System.arraycopy(paramBytes, 0, ivBytes, 0, len);

        return ivBytes;
    }
}