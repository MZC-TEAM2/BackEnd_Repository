package com.mzc.backend.lms.domains.user.auth.encryption;

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.auth.encryption.strategy.AES256EncryptionStrategy;
import com.mzc.backend.lms.domains.user.auth.encryption.strategy.BCryptEncryptionStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 암호화 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("암호화 서비스 테스트")
class EncryptionServiceTest {

    @InjectMocks
    private EncryptionService encryptionService;

    @Mock
    private AES256EncryptionStrategy aes256Strategy;

    @Mock
    private BCryptEncryptionStrategy bcryptStrategy;

    @Test
    @DisplayName("이메일 암호화 테스트")
    void encryptEmail() {
        // given
        String email = "test@example.com";
        String encryptedEmail = "encrypted_email";
        when(aes256Strategy.encrypt(email)).thenReturn(encryptedEmail);

        // when
        String result = encryptionService.encryptEmail(email);

        // then
        assertThat(result).isEqualTo(encryptedEmail);
        verify(aes256Strategy, times(1)).encrypt(email);
    }

    @Test
    @DisplayName("이메일 복호화 테스트")
    void decryptEmail() {
        // given
        String encryptedEmail = "encrypted_email";
        String email = "test@example.com";
        when(aes256Strategy.decrypt(encryptedEmail)).thenReturn(email);

        // when
        String result = encryptionService.decryptEmail(encryptedEmail);

        // then
        assertThat(result).isEqualTo(email);
        verify(aes256Strategy, times(1)).decrypt(encryptedEmail);
    }

    @Test
    @DisplayName("전화번호 암호화 테스트")
    void encryptPhoneNumber() {
        // given
        String phoneNumber = "010-1234-5678";
        String encryptedPhone = "encrypted_phone";
        when(aes256Strategy.encrypt(phoneNumber)).thenReturn(encryptedPhone);

        // when
        String result = encryptionService.encryptPhoneNumber(phoneNumber);

        // then
        assertThat(result).isEqualTo(encryptedPhone);
        verify(aes256Strategy, times(1)).encrypt(phoneNumber);
    }

    @Test
    @DisplayName("전화번호 복호화 테스트")
    void decryptPhoneNumber() {
        // given
        String encryptedPhone = "encrypted_phone";
        String phoneNumber = "010-1234-5678";
        when(aes256Strategy.decrypt(encryptedPhone)).thenReturn(phoneNumber);

        // when
        String result = encryptionService.decryptPhoneNumber(encryptedPhone);

        // then
        assertThat(result).isEqualTo(phoneNumber);
        verify(aes256Strategy, times(1)).decrypt(encryptedPhone);
    }

    @Test
    @DisplayName("비밀번호 암호화 테스트")
    void encryptPassword() {
        // given
        String password = "password123";
        String hashedPassword = "hashed_password";
        when(bcryptStrategy.encrypt(password)).thenReturn(hashedPassword);

        // when
        String result = encryptionService.encryptPassword(password);

        // then
        assertThat(result).isEqualTo(hashedPassword);
        verify(bcryptStrategy, times(1)).encrypt(password);
    }

    @Test
    @DisplayName("비밀번호 매칭 테스트 - 성공")
    void matchesPassword_Success() {
        // given
        String plainPassword = "password123";
        String hashedPassword = "hashed_password";
        when(bcryptStrategy.matches(plainPassword, hashedPassword)).thenReturn(true);

        // when
        boolean result = encryptionService.matchesPassword(plainPassword, hashedPassword);

        // then
        assertThat(result).isTrue();
        verify(bcryptStrategy, times(1)).matches(plainPassword, hashedPassword);
    }

    @Test
    @DisplayName("비밀번호 매칭 테스트 - 실패")
    void matchesPassword_Failure() {
        // given
        String plainPassword = "wrong_password";
        String hashedPassword = "hashed_password";
        when(bcryptStrategy.matches(plainPassword, hashedPassword)).thenReturn(false);

        // when
        boolean result = encryptionService.matchesPassword(plainPassword, hashedPassword);

        // then
        assertThat(result).isFalse();
        verify(bcryptStrategy, times(1)).matches(plainPassword, hashedPassword);
    }

    @Test
    @DisplayName("데이터 타입별 전략 선택 테스트 - 비밀번호")
    void getStrategy_Password() {
        // when
        var strategy = encryptionService.getStrategy(EncryptionService.DataType.PASSWORD);

        // then
        assertThat(strategy).isEqualTo(bcryptStrategy);
    }

    @Test
    @DisplayName("데이터 타입별 전략 선택 테스트 - 이메일")
    void getStrategy_Email() {
        // when
        var strategy = encryptionService.getStrategy(EncryptionService.DataType.EMAIL);

        // then
        assertThat(strategy).isEqualTo(aes256Strategy);
    }

    @Test
    @DisplayName("데이터 타입별 전략 선택 테스트 - 전화번호")
    void getStrategy_PhoneNumber() {
        // when
        var strategy = encryptionService.getStrategy(EncryptionService.DataType.PHONE_NUMBER);

        // then
        assertThat(strategy).isEqualTo(aes256Strategy);
    }

    @Test
    @DisplayName("개인정보 암호화 테스트")
    void encryptPersonalInfo() {
        // given
        String data = "personal_info";
        String encryptedData = "encrypted_personal_info";
        when(aes256Strategy.encrypt(data)).thenReturn(encryptedData);

        // when
        String result = encryptionService.encryptPersonalInfo(data);

        // then
        assertThat(result).isEqualTo(encryptedData);
        verify(aes256Strategy, times(1)).encrypt(data);
    }

    @Test
    @DisplayName("개인정보 복호화 테스트")
    void decryptPersonalInfo() {
        // given
        String encryptedData = "encrypted_personal_info";
        String data = "personal_info";
        when(aes256Strategy.decrypt(encryptedData)).thenReturn(data);

        // when
        String result = encryptionService.decryptPersonalInfo(encryptedData);

        // then
        assertThat(result).isEqualTo(data);
        verify(aes256Strategy, times(1)).decrypt(encryptedData);
    }
}