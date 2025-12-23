package com.mzc.backend.lms.domains.course.subject.service;

import com.mzc.backend.lms.domains.course.subject.dto.SubjectDetailResponse;
import com.mzc.backend.lms.domains.course.subject.dto.SubjectResponse;
import com.mzc.backend.lms.domains.course.subject.dto.SubjectSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 과목 서비스 인터페이스
 */
public interface SubjectService {
	
	/**
	 * 과목 목록 조회 (교수용 - 소속 학과 기본)
	 */
	Page<SubjectResponse> getSubjects(
			Long userId,
			String userType,
			String keyword,
			Long departmentId,
			Boolean showAllDepartments,
			String courseType,
			Integer credits,
			Boolean isActive,
			Pageable pageable
	);
	
	/**
	 * 과목 상세 조회
	 */
	SubjectDetailResponse getSubjectDetail(Long subjectId);
	
	/**
	 * 과목 검색 (페이징 지원)
	 */
	Page<SubjectSearchResponse> searchSubjects(String query, Pageable pageable);
}

