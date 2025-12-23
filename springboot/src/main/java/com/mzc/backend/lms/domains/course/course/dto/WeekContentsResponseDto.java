package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 주차별 콘텐츠 목록 응답 DTO
 */
@Getter
@Builder
public class WeekContentsResponseDto {
	private Long weekId;
	private Integer weekNumber;
	private String weekTitle;
	private List<WeekContentDto> contents;
}

