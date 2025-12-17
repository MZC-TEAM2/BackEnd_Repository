package com.mzc.backend.lms.domains.board.assignment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과제 제출 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionRequestDto {

    @NotBlank(message = "제출 내용은 필수입니다")
    private String content;
}
