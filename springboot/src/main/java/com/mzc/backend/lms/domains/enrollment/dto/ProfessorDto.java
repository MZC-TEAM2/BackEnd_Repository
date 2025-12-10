package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 교수 정보 DTO
 */
@Getter
@Builder
public class ProfessorDto {
    private Long id;
    private String name;
}
