package com.mzc.backend.lms.domains.assessment.dto.response;

import com.mzc.backend.lms.domains.assessment.entity.Assessment;
import com.mzc.backend.lms.domains.assessment.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AssessmentDetailResponseDto {

    private Long id;
    private Long postId;
    private Long courseId;
    private AssessmentType type;

    private String title;
    private String content;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer durationMinutes;
    private BigDecimal totalScore;
    private Boolean isOnline;
    private String location;
    private String instructions;
    private Integer questionCount;
    private BigDecimal passingScore;

    /** 문제 JSON (학생 응답에서는 정답 마스킹된 데이터) */
    private String questionData;

    public static AssessmentDetailResponseDto from(Assessment a, String questionData) {
        return AssessmentDetailResponseDto.builder()
                .id(a.getId())
                .postId(a.getPost().getId())
                .courseId(a.getCourseId())
                .type(a.getType())
                .title(a.getPost().getTitle())
                .content(a.getPost().getContent())
                .startAt(a.getStartAt())
                .endAt(a.endAt())
                .durationMinutes(a.getDurationMinutes())
                .totalScore(a.getTotalScore())
                .isOnline(a.getIsOnline())
                .location(a.getLocation())
                .instructions(a.getInstructions())
                .questionCount(a.getQuestionCount())
                .passingScore(a.getPassingScore())
                .questionData(questionData)
                .build();
    }
}


