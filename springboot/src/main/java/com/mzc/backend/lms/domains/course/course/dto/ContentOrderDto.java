package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 콘텐츠 순서 DTO
 */
@Getter
@Builder
public class ContentOrderDto {
    private Long contentId;
    private Integer order;
}

