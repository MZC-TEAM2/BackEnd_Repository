package com.mzc.backend.lms.domains.enrollment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 수강취소 완료 이벤트
 * 수강취소 성공 시 발행되어 알림 발송에 사용됨
 */
@Getter
@AllArgsConstructor
public class EnrollmentCancelledEvent {

    /**
     * 수강취소한 학생 ID
     */
    private final Long studentId;

    /**
     * 취소된 강의 ID
     */
    private final Long courseId;

    /**
     * 강의명
     */
    private final String courseName;

    /**
     * 분반 번호
     */
    private final String sectionNumber;
}
