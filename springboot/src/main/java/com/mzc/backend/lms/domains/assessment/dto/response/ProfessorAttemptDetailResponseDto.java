package com.mzc.backend.lms.domains.assessment.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProfessorAttemptDetailResponseDto {

    private Long attemptId;
    private Long examId;
    private Long courseId;

    private ProfessorAttemptListItemResponseDto.StudentInfo student;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    private Boolean isLate;
    private BigDecimal latePenaltyRate;

    private BigDecimal score;
    private String feedback;

    /**
     * 명세서 형식에 맞추기 위해 래핑:
     * { "answers": { "q1": 1, ... } }
     */
    private JsonNode answerData;

    /**
     * 교수 조회는 정답 포함 원본 제공
     */
    private JsonNode questionData;
}


