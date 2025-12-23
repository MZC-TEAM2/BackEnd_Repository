package com.mzc.backend.lms.views.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 교수 정보 통합 View 모델
 * <p>
 * Professor, User, UserProfile, UserContact, Department 등의 정보를 통합한 읽기 전용 모델입니다.
 * 모든 암호화된 필드는 복호화된 상태로 제공됩니다.
 */
@Getter
@Builder
public class ProfessorView {
	
	// ==================== Professor 정보 ====================
	private final Long userId;
	private final String professorNumber;
	private final LocalDate appointmentDate;  // 임용일자
	
	// ==================== UserProfile 정보 ====================
	private final String name;  // 복호화된 이름
	
	// ==================== UserContact 정보 ====================
	private final String phoneNumber;  // 복호화된 전화번호
	private final String email;  // 복호화된 이메일
	private final String officeNumber;  // 사무실 전화번호 (있는 경우)
	
	// ==================== Department 정보 ====================
	private final Long departmentId;
	private final String departmentName;
	private final Long collegeId;
	private final String collegeName;
	
	// ==================== UserProfileImage 정보 ====================
	private final String profileImageUrl;  // 현재 프로필 이미지 URL
	
	// ==================== 기타 정보 ====================
	private final LocalDate departmentJoinDate;  // 학과 배정일
	private final Boolean isDepartmentActive;  // 학과 활성 상태
	private final String position;  // 직급 (조교수, 부교수, 정교수 등)
	private final String officeLocation;  // 연구실 위치
	private final LocalDateTime createdAt;
	
	/**
	 * 표시용 이름 생성 (이름과 교번 조합)
	 *
	 * @return "홍길동 (P2024001)" 형식
	 */
	public String getDisplayName() {
		if (name != null && professorNumber != null) {
			return String.format("%s (%s)", name, professorNumber);
		}
		return name != null ? name : professorNumber;
	}
	
	/**
	 * 직급을 포함한 이름
	 *
	 * @return "홍길동 교수" 또는 "홍길동 조교수" 형식
	 */
	public String getNameWithTitle() {
		if (name != null) {
			if (position != null && !position.isEmpty()) {
				return String.format("%s %s", name, position);
			}
			return name + " 교수";
		}
		return "교수";
	}
	
	/**
	 * 근속 연수 계산
	 *
	 * @return 임용일로부터 현재까지의 연수
	 */
	public int getYearsOfService() {
		if (appointmentDate != null) {
			return LocalDate.now().getYear() - appointmentDate.getYear();
		}
		return 0;
	}
	
	/**
	 * 전체 학과 정보 텍스트
	 *
	 * @return "공과대학 컴퓨터공학과" 형식
	 */
	public String getFullDepartmentName() {
		if (collegeName != null && departmentName != null) {
			return String.format("%s %s", collegeName, departmentName);
		}
		return departmentName != null ? departmentName : "소속 없음";
	}
	
	/**
	 * 연구실 정보 텍스트
	 *
	 * @return "공학관 301호" 형식 또는 null
	 */
	public String getOfficeInfo() {
		if (officeLocation != null && !officeLocation.isEmpty()) {
			if (officeNumber != null && !officeNumber.isEmpty()) {
				return String.format("%s (내선: %s)", officeLocation, officeNumber);
			}
			return officeLocation;
		}
		return null;
	}
	
	/**
	 * 프로필 이미지 존재 여부
	 *
	 * @return 프로필 이미지가 있으면 true
	 */
	public boolean hasProfileImage() {
		return profileImageUrl != null && !profileImageUrl.isEmpty();
	}
	
	/**
	 * 연락 가능 여부
	 *
	 * @return 전화번호, 이메일, 또는 사무실 번호가 있으면 true
	 */
	public boolean isContactable() {
		return (phoneNumber != null && !phoneNumber.isEmpty()) ||
				(email != null && !email.isEmpty()) ||
				(officeNumber != null && !officeNumber.isEmpty());
	}
	
	/**
	 * 간단한 정보 문자열 생성 (로깅용)
	 *
	 * @return 주요 정보 요약
	 */
	@Override
	public String toString() {
		return String.format("ProfessorView{professorNumber='%s', name='%s', position='%s', department='%s'}",
				professorNumber, name, position, departmentName);
	}
}
