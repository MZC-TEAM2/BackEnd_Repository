package com.mzc.backend.lms.domains.user.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 프로필 조회 응답 DTO
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponseDto {
	
	// 기본 정보
	private final Long userId;
	private final String email;
	private final String name;
	
	// 연락처 정보
	private final String mobileNumber;
	private final String homeNumber;
	private final String officeNumber;
	private final Boolean mobileVerified;
	
	// 프로필 이미지
	private final String profileImageUrl;
	private final String thumbnailUrl;
	
	// 사용자 타입
	private final String userType;
	
	// 학생 정보 (userType이 STUDENT인 경우)
	private final Long studentId;
	private final Integer admissionYear;
	private final Integer grade;
	
	// 교수 정보 (userType이 PROFESSOR인 경우)
	private final Long professorId;
	private final LocalDate appointmentDate;
	
	// 학과 정보 (공통)
	private final String collegeName;
	private final String departmentName;
}
