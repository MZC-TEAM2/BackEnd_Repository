package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 수강 정원 정보 DTO (수강신청 상태 제외)
 */
@Getter
@Builder
public class EnrollmentInfoDto {
    private Integer current;
    private Integer max;
    private Boolean isFull;
    private Integer availableSeats;
}
