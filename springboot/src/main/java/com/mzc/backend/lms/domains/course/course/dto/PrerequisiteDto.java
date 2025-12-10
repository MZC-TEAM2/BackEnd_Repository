package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 선수과목 DTO
 */
@Getter
@Builder
public class PrerequisiteDto {
    private String subjectCode;
    private String subjectName;
    private Boolean isMandatory;  // 필수 여부
}
