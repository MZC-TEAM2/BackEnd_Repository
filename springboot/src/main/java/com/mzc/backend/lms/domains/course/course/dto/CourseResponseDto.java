package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * 강의 목록 응답 DTO (수강신청 정보 제외)
 */
@Getter
@Builder
public class CourseResponseDto {
    private List<CourseDto> content;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
}
