package com.mzc.backend.lms.domains.user.auth.service;

import com.mzc.backend.lms.domains.user.auth.dto.*;
import com.mzc.backend.lms.domains.user.auth.usecase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 인증 서비스 Facade
 * UseCase 패턴을 통해 각 비즈니스 로직을 위임
 *
 * 이 서비스는 컨트롤러와 UseCase 사이의 중간 계층 역할을 하며,
 * 하위 호환성을 유지하면서 점진적으로 UseCase 패턴으로 마이그레이션
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final CheckEmailUseCase checkEmailUseCase;

    /**
     * 회원가입
     *
     * @param dto 회원가입 요청 정보
     * @return 생성된 사용자 ID
     */
    public Long signup(SignupRequestDto dto) {
        log.debug("회원가입 요청: email={}", dto.getEmail());
        return signupUseCase.execute(dto);
    }

    /**
     * 로그인
     *
     * @param dto 로그인 요청 정보
     * @param ipAddress 클라이언트 IP 주소
     * @return 로그인 응답 (토큰 포함)
     */
    public LoginResponseDto login(LoginRequestDto dto, String ipAddress) {
        log.debug("로그인 요청: username={}", dto.getUsername());
        return loginUseCase.execute(dto, ipAddress);
    }

    /**
     * 토큰 갱신
     *
     * @param dto Refresh Token 요청 정보
     * @return 새로운 토큰 응답
     */
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto dto) {
        log.debug("토큰 갱신 요청");
        return refreshTokenUseCase.execute(dto);
    }

    /**
     * 로그아웃
     *
     * @param refreshToken 폐기할 Refresh Token
     */
    public void logout(String refreshToken) {
        log.debug("로그아웃 요청");
        logoutUseCase.execute(refreshToken);
    }

    /**
     * 이메일 사용 가능 여부 확인
     *
     * @param email 확인할 이메일
     * @return true: 사용 가능, false: 사용 불가
     */
    public boolean isEmailAvailable(String email) {
        log.debug("이메일 중복 확인: email={}", email);
        return checkEmailUseCase.execute(email);
    }
}