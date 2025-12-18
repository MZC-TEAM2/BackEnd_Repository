package com.mzc.backend.lms.domains.board.assignment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 과제 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCreateRequestDto {

    // Post 정보
    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    private List<String> hashtags;

    private List<Long> attachmentIds;

    // Assignment 정보
    @NotNull(message = "강의 ID는 필수입니다")
    private Long courseId;

    @NotNull(message = "마감일은 필수입니다")
    private LocalDateTime dueDate;

    @NotNull(message = "만점은 필수입니다")
    private BigDecimal maxScore;

    @NotBlank(message = "제출 방법은 필수입니다")
    private String submissionMethod; // UPLOAD, TEXT, LINK

    @Builder.Default
    private Boolean lateSubmissionAllowed = false;

    private BigDecimal latePenaltyPercent;

    private Integer maxFileSizeMb;

    private String allowedFileTypes; // comma-separated: "pdf,docx,hwp"

    private String instructions;
}
