package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 이수구분 정보 DTO
 */
@Getter
@Builder
public class CourseTypeDto {
    private String code;
    private String name;
    private String color;
}
