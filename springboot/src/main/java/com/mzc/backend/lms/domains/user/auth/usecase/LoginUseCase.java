package com.mzc.backend.lms.domains.user.auth.usecase;

import com.mzc.backend.lms.domains.user.auth.dto.LoginRequestDto;
import com.mzc.backend.lms.domains.user.auth.dto.LoginResponseDto;

/**
 * 로그인 유스케이스 인터페이스
 *
 * 책임:
 * - 사용자 인증 처리
 * - JWT 토큰 생성
 * - Refresh Token 저장
 * - 로그인 이력 관리
 */
public interface LoginUseCase {

    /**
     * 로그인 처리
     *
     * @param dto 로그인 요청 정보
     * @param ipAddress 클라이언트 IP 주소
     * @return 로그인 응답 (토큰 포함)
     * @throws IllegalArgumentException 인증 실패 시
     * @throws RuntimeException 로그인 처리 중 오류 발생 시
     */
    LoginResponseDto execute(LoginRequestDto dto, String ipAddress);

    /**
     * 사용자명으로 인증 가능 여부 확인
     * 이메일, 학번, 교번 모두 지원
     *
     * @param username 사용자명
     * @return true: 인증 가능, false: 불가능
     */
    boolean isAuthenticatable(String username);
}