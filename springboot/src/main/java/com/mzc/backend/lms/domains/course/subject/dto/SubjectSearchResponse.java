package com.mzc.backend.lms.domains.course.subject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과목 간단 검색 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectSearchResponse {
    private Long id;
    private String subjectCode;
    private String subjectName;
    private Integer credits;
    private String courseType;  // 간단한 이름만
    private String department;  // 간단한 이름만
}

