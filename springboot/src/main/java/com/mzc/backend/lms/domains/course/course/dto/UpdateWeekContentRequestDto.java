package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 주차별 콘텐츠 수정 요청 DTO
 */
@Getter
@Builder
public class UpdateWeekContentRequestDto {
    private String contentType;
    private String title;
    private String contentUrl;
    private String duration;
    private Integer displayOrder;
}

