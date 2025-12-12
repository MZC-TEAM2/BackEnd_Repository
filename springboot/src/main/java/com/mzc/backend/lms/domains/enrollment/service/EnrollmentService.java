package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentBulkRequestDto;
import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentBulkResponseDto;

/**
 * 수강신청 서비스 인터페이스
 */
public interface EnrollmentService {
    /**
     * 일괄 수강신청
     * @param request 수강신청 요청 (courseIds)
     * @param studentId 학생 ID
     * @return 수강신청 결과 (부분 성공 허용)
     */
    EnrollmentBulkResponseDto enrollBulk(EnrollmentBulkRequestDto request, String studentId);
}
