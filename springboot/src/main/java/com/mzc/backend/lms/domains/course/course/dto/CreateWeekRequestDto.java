package com.mzc.backend.lms.domains.course.course.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주차 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWeekRequestDto {
    @JsonProperty("weekNumber")
    private Integer weekNumber;
    
    @JsonProperty("weekTitle")
    private String weekTitle;
    
    @JsonProperty("contents")
    private List<CreateWeekContentRequestDto> contents;  // 콘텐츠 배열 (선택)
}

