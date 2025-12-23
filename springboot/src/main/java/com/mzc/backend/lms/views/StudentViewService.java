package com.mzc.backend.lms.views;

import com.mzc.backend.lms.domains.user.user.exceptions.UserException;
import com.mzc.backend.lms.views.user.StudentView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 학생 정보 통합 조회 서비스 인터페이스
 * <p>
 * Student, User, UserProfile, UserContact, Department 등 여러 테이블에 분산된
 * 학생 정보를 통합하여 조회하는 서비스입니다.
 * <p>
 * 모든 암호화된 필드(이름, 전화번호 등)는 자동으로 복호화되어 반환됩니다.
 */
public interface StudentViewService {
	
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
	 * 여러 학번으로 학생 정보 일괄 조회
	 * N+1 문제를 방지하기 위해 배치로 조회합니다.
	 *
	 * @param studentNumbers 학번 목록
	 * @return 학번을 키로 하는 학생 정보 맵
	 */
	Map<String, StudentView> getStudentsByNumbers(List<String> studentNumbers);
	
	
	/**
	 * 학번 존재 여부 확인
	 *
	 * @param studentNumber 학번
	 * @return 존재하면 true, 없으면 false
	 */
	boolean existsByStudentNumber(String studentNumber);
}
