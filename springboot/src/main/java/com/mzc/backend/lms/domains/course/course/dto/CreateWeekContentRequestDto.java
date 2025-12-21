package com.mzc.backend.lms.domains.course.course.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주차별 콘텐츠 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWeekContentRequestDto {
    @JsonProperty("contentType")
    private String contentType; // VIDEO, DOCUMENT, LINK, QUIZ - 필수
    
    @JsonProperty("title")
    private String title;  // 필수
    
    @JsonProperty("contentUrl")
    private String contentUrl;  // 필수
    
    @JsonProperty("duration")
    private String duration; // 동영상인 경우만 (예: "45:23" 또는 "HH:MM:SS")
    
    @JsonProperty("order")
    private Integer order;  // 콘텐츠 순서 (기본값: 마지막 순서)
}

