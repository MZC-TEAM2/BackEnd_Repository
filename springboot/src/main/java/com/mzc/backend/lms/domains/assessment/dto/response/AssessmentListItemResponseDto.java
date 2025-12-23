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
public class AssessmentListItemResponseDto {

    private Long id;
    private Long courseId;
    private AssessmentType type;
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer durationMinutes;
    private BigDecimal totalScore;
    private Boolean isOnline;
    private String location;

    public static AssessmentListItemResponseDto from(Assessment a) {
        return AssessmentListItemResponseDto.builder()
                .id(a.getId())
                .courseId(a.getCourseId())
                .type(a.getType())
                .title(a.getPost().getTitle())
                .startAt(a.getStartAt())
                .endAt(a.endAt())
                .durationMinutes(a.getDurationMinutes())
                .totalScore(a.getTotalScore())
                .isOnline(a.getIsOnline())
                .location(a.getLocation())
                .build();
    }
}


