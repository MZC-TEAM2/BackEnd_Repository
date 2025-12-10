package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 강의 유형 DTO
 */
@Getter
@Builder
public class CourseTypeDto {
    private String code; // MAJOR_REQ, MAJOR_ELEC, GEN_REQ, GEN_ELEC
    private String name; // 전공필수, 전공선택, 교양필수, 교양선택
    private String color;
}
