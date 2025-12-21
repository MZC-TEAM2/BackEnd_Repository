package com.mzc.backend.lms.domains.user.auth.token.scheduler;

import com.mzc.backend.lms.domains.user.auth.token.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenCleanupScheduler 테스트")
class RefreshTokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCleanupScheduler scheduler;

    @Test
    @DisplayName("만료/폐기 토큰 정리 스케줄러가 정상 실행된다")
    void givenExpiredOrRevokedTokens_whenCleanup_thenTokensDeleted() {
        // Given
        given(refreshTokenRepository.deleteExpiredOrRevokedTokens(any(LocalDateTime.class)))
                .willReturn(5);

        // When
        scheduler.cleanExpiredOrRevokedTokens();

        // Then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(refreshTokenRepository).deleteExpiredOrRevokedTokens(captor.capture());

        LocalDateTime capturedTime = captor.getValue();
        assertThat(capturedTime).isNotNull();
        assertThat(capturedTime).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("삭제할 토큰이 없어도 정상 실행된다")
    void givenNoTokensToDelete_whenCleanup_thenCompletesSuccessfully() {
        // Given
        given(refreshTokenRepository.deleteExpiredOrRevokedTokens(any(LocalDateTime.class)))
                .willReturn(0);

        // When
        scheduler.cleanExpiredOrRevokedTokens();

        // Then
        verify(refreshTokenRepository).deleteExpiredOrRevokedTokens(any(LocalDateTime.class));
    }
}
