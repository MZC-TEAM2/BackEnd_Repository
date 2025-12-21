package com.mzc.backend.lms.domains.course.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 강의계획서 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusRequestDto {
    private List<String> objectives;  // 학습 목표
    private String textbook;           // 교재
    private GradingRequestDto grading; // 평가 기준
}

