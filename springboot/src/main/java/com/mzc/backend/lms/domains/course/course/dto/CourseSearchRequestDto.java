package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 강의 검색 요청 DTO
 */
@Getter
@Builder
public class CourseSearchRequestDto {
    private Integer page;
    private Integer size;
    private String keyword;
    private Long departmentId;
    private Integer courseType;
    private Integer credits;
    private Long termId;
    private String sort;
}
