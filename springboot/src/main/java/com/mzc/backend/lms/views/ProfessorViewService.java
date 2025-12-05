package com.mzc.backend.lms.views;

import com.mzc.backend.lms.domains.user.exceptions.UserException;
import com.mzc.backend.lms.views.user.ProfessorView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 교수 정보 통합 조회 서비스 인터페이스
 *
 * Professor, User, UserProfile, UserContact, Department 등 여러 테이블에 분산된
 * 교수 정보를 통합하여 조회하는 서비스입니다.
 *
 * 모든 암호화된 필드(이름, 전화번호 등)는 자동으로 복호화되어 반환됩니다.
 */
public interface ProfessorViewService {

    /**
     * 교번으로 교수 정보 조회
     *
     * @param professorNumber 교번
     * @return 교수 상세 정보 (프로필, 학과, 프로필 이미지 포함)
     * @throws UserException 교수를 찾을 수 없는 경우
     */
    ProfessorView getProfessor(String professorNumber);

    /**
     * 교번으로 교수 정보 조회 (Optional)
     *
     * @param professorNumber 교번
     * @return 교수 정보 Optional
     */
    Optional<ProfessorView> findProfessor(String professorNumber);

    /**
     * User ID로 교수 정보 조회
     *
     * @param userId 사용자 ID
     * @return 교수 상세 정보
     * @throws UserException 교수를 찾을 수 없는 경우
     */
    ProfessorView getProfessorByUserId(Long userId);

    /**
     * User ID로 교수 정보 조회 (Optional)
     *
     * @param userId 사용자 ID
     * @return 교수 정보 Optional
     */
    Optional<ProfessorView> findProfessorByUserId(Long userId);

    /**
     * 여러 교번으로 교수 정보 일괄 조회
     *
     * @param professorNumbers 교번 목록
     * @return 교번을 키로 하는 교수 정보 맵
     */
    Map<String, ProfessorView> getProfessorsByNumbers(List<String> professorNumbers);

    /**
     * 여러 User ID로 교수 정보 일괄 조회
     *
     * @param userIds User ID 목록
     * @return User ID를 키로 하는 교수 정보 맵
     */
    Map<Long, ProfessorView> getProfessorsByUserIds(List<Long> userIds);

    /**
     * User ID로 교수 여부 확인
     *
     * @param userId User ID
     * @return 교수이면 true, 아니면 false
     */
    boolean isProfessor(Long userId);

    /**
     * 교번 존재 여부 확인
     *
     * @param professorNumber 교번
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByProfessorNumber(String professorNumber);
}