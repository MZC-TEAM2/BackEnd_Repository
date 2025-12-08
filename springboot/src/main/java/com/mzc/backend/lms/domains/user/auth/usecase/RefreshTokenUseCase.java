package com.mzc.backend.lms.domains.user.auth.usecase;

import com.mzc.backend.lms.domains.user.auth.dto.RefreshTokenRequestDto;
import com.mzc.backend.lms.domains.user.auth.dto.RefreshTokenResponseDto;

/**
 * 토큰 갱신 유스케이스 인터페이스
 *
 * 책임:
 * - Refresh Token 검증
 * - 새로운 Access/Refresh Token 발급
 * - Token Rotation 처리
 * - 기존 토큰 폐기
 */
public interface RefreshTokenUseCase {

    /**
     * 토큰 갱신 처리
     *
     * @param dto Refresh Token 요청 정보
     * @return 새로운 토큰 응답
     * @throws IllegalArgumentException 토큰 검증 실패 시
     * @throws RuntimeException 토큰 갱신 중 오류 발생 시
     */
    RefreshTokenResponseDto execute(RefreshTokenRequestDto dto);

    /**
     * Refresh Token 유효성 검증
     *
     * @param refreshToken 검증할 토큰
     * @return true: 유효, false: 무효
     */
    boolean isTokenValid(String refreshToken);

    /**
     * 특정 사용자의 모든 Refresh Token 폐기
     * 보안 이슈 발생 시 사용
     *
     * @param userId 사용자 ID (학번 또는 교번)
     * @return 폐기된 토큰 수
     */
    int revokeAllUserTokens(String userId);
}