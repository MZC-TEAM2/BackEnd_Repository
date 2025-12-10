package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 강의 스케줄 DTO
 */
@Getter
@Builder
public class ScheduleDto {
    private Integer dayOfWeek;
    private String dayName;
    private String startTime;
    private String endTime;
    private String classroom;
}
