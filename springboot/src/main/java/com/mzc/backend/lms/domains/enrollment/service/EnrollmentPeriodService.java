package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentPeriodResponseDto;

/**
 * 수강신청 기간 서비스
 */
public interface EnrollmentPeriodService {
    /**
     * 현재 활성화된 기간 조회
     * @param typeCode 기간 타입 코드 (ENROLLMENT, COURSE_REGISTRATION, ADJUSTMENT, CANCELLATION)
     *                 null이면 기본값으로 ENROLLMENT 조회
     */
    EnrollmentPeriodResponseDto getCurrentPeriod(String typeCode);
    
    /**
     * 현재 수강신청 기간 조회 (기본값: ENROLLMENT)
     * @deprecated getCurrentPeriod(null) 사용 권장
     */
    @Deprecated
    default EnrollmentPeriodResponseDto getCurrentEnrollmentPeriod() {
        return getCurrentPeriod("ENROLLMENT");
    }
}
