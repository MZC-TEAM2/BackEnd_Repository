package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

/**
 * 학기 정보 DTO
 */
@Getter
@Builder
public class AcademicTermDto {
    private Long id;
    private Integer year;
    private String termType;
    private LocalDate startDate;
    private LocalDate endDate;
}
