package com.mzc.backend.lms.domains.assessment.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.mzc.backend.lms.domains.assessment.enums.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentCreateRequestDto {

    @NotNull(message = "강의 ID는 필수입니다")
    private Long courseId;

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "설명은 필수입니다")
    private String content;

    @NotNull(message = "유형은 필수입니다")
    private AssessmentType type;

    @NotNull(message = "시작 시간은 필수입니다")
    private LocalDateTime startAt;

    @NotNull(message = "제한 시간(분)은 필수입니다")
    private Integer durationMinutes;

    @NotNull(message = "총점은 필수입니다")
    private BigDecimal totalScore;

    @Builder.Default
    private Boolean isOnline = false;

    private String location;
    private String instructions;
    private Integer questionCount;
    private BigDecimal passingScore;

    /**
     * 문제 JSON (정답 포함)
     */
    @NotNull(message = "문제 JSON은 필수입니다")
    private JsonNode questionData;
}


