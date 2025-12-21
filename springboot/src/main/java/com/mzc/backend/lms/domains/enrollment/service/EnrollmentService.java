package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.enrollment.dto.CourseIdsRequestDto;
import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentBulkCancelRequestDto;
import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentBulkCancelResponseDto;
import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentBulkResponseDto;
import com.mzc.backend.lms.domains.enrollment.dto.MyEnrollmentsResponseDto;

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
    EnrollmentBulkResponseDto enrollBulk(CourseIdsRequestDto request, String studentId);

    /**
     * 내 수강신청 목록 조회
     * @param studentId 학생 ID
     * @param enrollmentPeriodId 수강신청 기간 ID (선택, null이면 현재 학기)
     * @return 수강신청 목록
     */
    MyEnrollmentsResponseDto getMyEnrollments(String studentId, Long enrollmentPeriodId);

    /**
     * 일괄 수강신청 취소
     * @param request 수강신청 취소 요청 (enrollmentIds)
     * @param studentId 학생 ID
     * @return 수강신청 취소 결과 (부분 성공 허용)
     */
    EnrollmentBulkCancelResponseDto cancelBulk(EnrollmentBulkCancelRequestDto request, String studentId);
}
