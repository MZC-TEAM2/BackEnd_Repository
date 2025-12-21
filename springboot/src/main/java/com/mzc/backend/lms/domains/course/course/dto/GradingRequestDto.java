package com.mzc.backend.lms.domains.course.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 평가 기준 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingRequestDto {
    private Integer midterm;        // 중간고사 비율
    @com.fasterxml.jackson.annotation.JsonProperty("final")
    private Integer finalExam;       // 기말고사 비율 (API에서 "final"로 오므로)
    private Integer assignment;      // 과제 비율
    private Integer attendance;      // 출석 비율
    private Integer participation;    // 참여도 비율
}

