package com.mzc.backend.lms.domains.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AttemptGradeResponseDto {

    private Long attemptId;
    private BigDecimal score;
    private Boolean isLate;
    private BigDecimal latePenaltyRate;
    private LocalDateTime gradedAt;
    private Long gradedBy;
}


