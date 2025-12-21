package com.mzc.backend.lms.domains.user.search.dto;

import com.mzc.backend.lms.domains.user.organization.entity.College;
import lombok.Builder;
import lombok.Getter;

/**
 * 단과대 목록 응답 DTO (필터 선택용)
 */
@Getter
@Builder
public class CollegeListResponseDto {

    private Long id;

    private String collegeName;

    private String collegeCode;

    public static CollegeListResponseDto from(College college) {
        return CollegeListResponseDto.builder()
                .id(college.getId())
                .collegeName(college.getCollegeName())
                .collegeCode(college.getCollegeCode())
                .build();
    }
}
