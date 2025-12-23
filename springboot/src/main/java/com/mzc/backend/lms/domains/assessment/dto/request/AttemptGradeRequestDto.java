package com.mzc.backend.lms.domains.assessment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 교수 채점 요청 DTO (시험용)
 * - score는 "원점수(raw)"로 받고, latePenaltyRate가 있으면 서버에서 감점 적용 후 저장
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptGradeRequestDto {

    @NotNull(message = "점수는 필수입니다")
    private BigDecimal score;

    private String feedback;
}


