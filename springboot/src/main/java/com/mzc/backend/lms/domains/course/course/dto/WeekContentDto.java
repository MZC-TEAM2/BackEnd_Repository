package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주차별 콘텐츠 응답 DTO
 */
@Getter
@Builder
public class WeekContentDto {
    private Long id;
    private String contentType;
    private String title;
    private String contentUrl;
    private String duration;
    private Integer order;  // 표시 순서
    private LocalDateTime createdAt;
}

