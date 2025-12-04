package com.mzc.backend.lms.domains.user.auth.usecase;

/**
 * 이메일 확인 유스케이스 인터페이스
 *
 * 책임:
 * - 이메일 중복 확인
 * - 이메일 형식 검증
 * - 이메일 도메인 검증 (학교 도메인 등)
 */
public interface CheckEmailUseCase {

    /**
     * 이메일 사용 가능 여부 확인
     *
     * @param email 확인할 이메일
     * @return true: 사용 가능, false: 사용 불가
     */
    boolean execute(String email);

    /**
     * 이메일 형식 유효성 검증
     *
     * @param email 검증할 이메일
     * @return true: 유효한 형식, false: 잘못된 형식
     */
    boolean isValidFormat(String email);


}
