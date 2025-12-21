package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주차 목록 응답 DTO (교수용)
 */
@Getter
@Builder
public class WeekListResponseDto {
    private Long id;
    private Integer weekNumber;
    private String weekTitle;
    private List<WeekContentDto> contents;
    private LocalDateTime createdAt;
}

