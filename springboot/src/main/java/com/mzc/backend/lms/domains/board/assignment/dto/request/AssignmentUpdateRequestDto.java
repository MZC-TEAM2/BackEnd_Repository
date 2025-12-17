package com.mzc.backend.lms.domains.board.assignment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 과제 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentUpdateRequestDto {

    // Post 정보
    private String title;

    private String content;

    // Assignment 정보
    private LocalDateTime dueDate;

    private BigDecimal maxScore;

    private String submissionMethod;

    private Boolean lateSubmissionAllowed;

    private BigDecimal latePenaltyPercent;

    private Integer maxFileSizeMb;

    private String allowedFileTypes;

    private String instructions;
}
