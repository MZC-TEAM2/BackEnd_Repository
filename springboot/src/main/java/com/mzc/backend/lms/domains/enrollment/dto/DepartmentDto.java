package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 학과 정보 DTO
 */
@Getter
@Builder
public class DepartmentDto {
    private Long id;
    private String name;
}
