package com.mzc.backend.lms.domains.course.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 스케줄 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDto {
    private Integer dayOfWeek; // 1=월요일, 2=화요일, ...
    private String startTime; // "09:00"
    private String endTime; // "10:30"
    private String classroom;
}

