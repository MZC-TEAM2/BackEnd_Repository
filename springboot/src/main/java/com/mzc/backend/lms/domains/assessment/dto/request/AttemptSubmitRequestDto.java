package com.mzc.backend.lms.domains.assessment.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptSubmitRequestDto {

    /**
     * 답안 JSON (문제별 답안 전체)
     */
    @NotNull(message = "답안은 필수입니다")
    private JsonNode answers;
}


