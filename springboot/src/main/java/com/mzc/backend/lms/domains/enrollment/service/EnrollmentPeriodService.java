package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentPeriodResponseDto;

/**
 * 수강신청 기간 서비스
 */
public interface EnrollmentPeriodService {
    /**
     * 현재 수강신청 기간 조회
     */
    EnrollmentPeriodResponseDto getCurrentEnrollmentPeriod();
}
