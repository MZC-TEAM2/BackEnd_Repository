package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 주차별 강의 계획 DTO
 */
@Getter
@Builder
public class WeekDto {
    private Long id;
    private Integer weekNumber;
    private String weekTitle;
}
