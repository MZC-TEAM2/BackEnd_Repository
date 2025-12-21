package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.enrollment.dto.CourseListResponseDto;
import com.mzc.backend.lms.domains.enrollment.dto.CourseSearchRequestDto;

public interface EnrollmentCourseService {
    /**
     * 강의 목록 조회 (검색 및 필터링)
     */
    CourseListResponseDto searchCourses(CourseSearchRequestDto request, String studentId);
}
