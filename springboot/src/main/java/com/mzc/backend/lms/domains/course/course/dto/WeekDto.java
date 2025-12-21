package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주차별 강의 계획 DTO
 */
@Getter
@Builder
public class WeekDto {
    private Long id;
    private Integer weekNumber;
    private String weekTitle;
    private List<WeekContentDto> contents;  // 주차 등록 응답에 포함
    private LocalDateTime createdAt;
}
