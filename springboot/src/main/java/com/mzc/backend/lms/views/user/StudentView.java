package com.mzc.backend.lms.views.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학생 정보 통합 View 모델
 *
 * Student, User, UserProfile, UserContact, Department 등의 정보를 통합한 읽기 전용 모델입니다.
 * 모든 암호화된 필드는 복호화된 상태로 제공됩니다.
 */
@Getter
@Builder
public class StudentView {

    // ==================== Student 정보 ====================
    private final Long userId;
    private final String studentNumber;
    private final Integer grade;
    private final Integer admissionYear;

    // ==================== UserProfile 정보 ====================
    private final String name;  // 복호화된 이름

    // ==================== UserContact 정보 ====================
    private final String phoneNumber;  // 복호화된 전화번호
    private final String email;  // 복호화된 이메일

    // ==================== Department 정보 ====================
    private final Long departmentId;
    private final String departmentName;
    private final Long collegeId;
    private final String collegeName;

    // ==================== UserProfileImage 정보 ====================
    private final String profileImageUrl;  // 현재 프로필 이미지 URL

    // ==================== 기타 정보 ====================
    private final LocalDate enrollmentDate;  // 학과 등록일
    private final Boolean isDepartmentActive;  // 학과 활성 상태
    private final LocalDateTime createdAt;

    /**
     * 표시용 이름 생성 (이름과 학번 조합)
     *
     * @return "홍길동 (2024001)" 형식
     */
    public String getDisplayName() {
        if (name != null && studentNumber != null) {
            return String.format("%s (%s)", name, studentNumber);
        }
        return name != null ? name : studentNumber;
    }

    /**
     * 학년 텍스트 생성
     *
     * @return "1학년" 형식
     */
    public String getGradeText() {
        return grade != null ? grade + "학년" : "미정";
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
     * @return 전화번호 또는 이메일이 있으면 true
     */
    public boolean isContactable() {
        return (phoneNumber != null && !phoneNumber.isEmpty()) ||
               (email != null && !email.isEmpty());
    }

    /**
     * 간단한 정보 문자열 생성 (로깅용)
     *
     * @return 주요 정보 요약
     */
    @Override
    public String toString() {
        return String.format("StudentView{studentNumber='%s', name='%s', grade=%d, department='%s'}",
            studentNumber, name, grade, departmentName);
    }
}