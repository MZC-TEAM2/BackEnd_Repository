package com.mzc.backend.lms.domains.board.assignment.dto.response;

import com.mzc.backend.lms.domains.board.assignment.entity.AssignmentSubmission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 과제 제출 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class AssignmentSubmissionResponseDto {

    private Long id;
    private Long assignmentId;
    private Long userId;
    private String content;
    private LocalDateTime submittedAt;
    private String status;
    private BigDecimal score;
    private String feedback;
    private LocalDateTime gradedAt;
    private Long gradedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssignmentSubmissionResponseDto from(AssignmentSubmission submission) {
        return AssignmentSubmissionResponseDto.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .userId(submission.getUserId())
                .content(submission.getContent())
                .submittedAt(submission.getSubmittedAt())
                .status(submission.getStatus())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .gradedAt(submission.getGradedAt())
                .gradedBy(submission.getGradedBy())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
