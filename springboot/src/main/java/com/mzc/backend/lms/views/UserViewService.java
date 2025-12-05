package com.mzc.backend.lms.views;

import com.mzc.backend.lms.domains.user.exceptions.UserException;
import com.mzc.backend.lms.views.user.ProfessorView;
import com.mzc.backend.lms.views.user.StudentView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 사용자 정보 통합 조회 서비스 인터페이스
 *
 * User, UserProfile, UserContact, Department 등 여러 테이블에 분산된
 * 사용자 정보를 통합하여 조회하는 서비스입니다.
 *
 * 모든 암호화된 필드(이름, 전화번호 등)는 자동으로 복호화되어 반환됩니다.
 */
public interface UserViewService {

    // ==================== 학생 조회 ====================

    /**
     * 학번으로 학생 정보 조회
     *
     * @param studentNumber 학번
     * @return 학생 상세 정보 (프로필, 학과, 프로필 이미지 포함)
     * @throws UserException 학생을 찾을 수 없는 경우
     */
    StudentView getStudent(String studentNumber);

    /**
     * 학번으로 학생 정보 조회 (Optional)
     *
     * @param studentNumber 학번
     * @return 학생 정보 Optional
     */
    Optional<StudentView> findStudent(String studentNumber);

    /**
     * User ID로 학생 정보 조회
     *
     * @param userId 사용자 ID
     * @return 학생 상세 정보
     * @throws UserException 학생을 찾을 수 없는 경우
     */
    StudentView getStudentByUserId(Long userId);

    /**
     * User ID로 학생 정보 조회 (Optional)
     *
     * @param userId 사용자 ID
     * @return 학생 정보 Optional
     */
    Optional<StudentView> findStudentByUserId(Long userId);

    /**
     * 여러 학번으로 학생 정보 일괄 조회
     * N+1 문제를 방지하기 위해 배치로 조회합니다.
     *
     * @param studentNumbers 학번 목록
     * @return 학번을 키로 하는 학생 정보 맵
     */
    Map<String, StudentView> getStudentsByNumbers(List<String> studentNumbers);

    /**
     * 여러 User ID로 학생 정보 일괄 조회
     *
     * @param userIds User ID 목록
     * @return User ID를 키로 하는 학생 정보 맵
     */
    Map<Long, StudentView> getStudentsByUserIds(List<Long> userIds);

    // ==================== 교수 조회 ====================

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

    // ==================== 공통 유틸리티 ====================

    /**
     * User ID로 사용자 타입 확인
     *
     * @param userId User ID
     * @return "STUDENT" 또는 "PROFESSOR", 없으면 null
     */
    String getUserType(Long userId);

    /**
     * User ID로 사용자 이름만 간단히 조회
     * 캐싱 가능한 간단한 조회용
     *
     * @param userId User ID
     * @return 복호화된 이름, 없으면 null
     */
    String getUserName(Long userId);

    /**
     * 여러 User ID로 이름 일괄 조회
     *
     * @param userIds User ID 목록
     * @return User ID를 키로 하는 이름 맵
     */
    Map<Long, String> getUserNames(List<Long> userIds);
}