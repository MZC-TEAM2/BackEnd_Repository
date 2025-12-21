package com.mzc.backend.lms.domains.board.assignment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 과제 채점 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentGradeRequestDto {

    @NotNull(message = "점수는 필수입니다")
    private BigDecimal score;

    private String feedback;
}
