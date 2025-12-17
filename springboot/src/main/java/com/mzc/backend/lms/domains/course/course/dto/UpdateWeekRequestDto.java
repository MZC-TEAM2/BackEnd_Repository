package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 주차 수정 요청 DTO
 */
@Getter
@Builder
public class UpdateWeekRequestDto {
    private Integer weekNumber;
    private String weekTitle;
}

