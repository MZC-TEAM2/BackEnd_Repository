package com.mzc.backend.lms.domains.user.auth.usecase;

import com.mzc.backend.lms.domains.user.auth.dto.SignupRequestDto;

/**
 * 회원가입 유스케이스 인터페이스
 *
 * 책임:
 * - 회원가입 비즈니스 로직 처리
 * - 이메일 인증 확인
 * - 사용자 타입별 엔티티 생성 (Student/Professor)
 * - 트랜잭션 경계 관리
 */
public interface SignupUseCase {

    /**
     * 회원가입 처리
     *
     * @param dto 회원가입 요청 정보
     * @return 생성된 사용자 ID (학번 또는 교번)
     * @throws IllegalArgumentException 유효성 검증 실패 시
     * @throws RuntimeException 회원가입 처리 중 오류 발생 시
     */
    String execute(SignupRequestDto dto);

    /**
     * 이메일 사용 가능 여부 확인
     *
     * @param email 확인할 이메일
     * @return true: 사용 가능, false: 이미 사용 중
     */
    boolean isEmailAvailable(String email);
}