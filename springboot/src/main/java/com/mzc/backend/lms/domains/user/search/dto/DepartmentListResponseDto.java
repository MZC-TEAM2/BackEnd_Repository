package com.mzc.backend.lms.domains.user.search.dto;

import com.mzc.backend.lms.domains.user.organization.entity.Department;
import lombok.Builder;
import lombok.Getter;

/**
 * 학과 목록 응답 DTO (필터 선택용)
 */
@Getter
@Builder
public class DepartmentListResponseDto {

    private Long id;

    private String departmentName;

    private String departmentCode;

    private Long collegeId;

    public static DepartmentListResponseDto from(Department department) {
        return DepartmentListResponseDto.builder()
                .id(department.getId())
                .departmentName(department.getDepartmentName())
                .departmentCode(department.getDepartmentCode())
                .collegeId(department.getCollege().getId())
                .build();
    }
}
