package com.mzc.backend.lms.domains.user.auth.usecase;

/**
 * 로그아웃 유스케이스 인터페이스
 *
 * 책임:
 * - Refresh Token 폐기
 * - 로그아웃 이력 기록
 * - 세션 정리
 */
public interface LogoutUseCase {

    /**
     * 로그아웃 처리
     *
     * @param refreshToken 폐기할 Refresh Token
     * @return 로그아웃 성공 여부
     */
    boolean execute(String refreshToken);


}
