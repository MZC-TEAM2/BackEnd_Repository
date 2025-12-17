package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 콘텐츠 순서 변경 응답 DTO
 */
@Getter
@Builder
public class ReorderContentsResponseDto {
    private Long weekId;
    private List<ContentOrderDto> reorderedContents;
}

