package com.mzc.backend.lms.domains.board.assignment.dto.response;

import com.mzc.backend.lms.domains.board.assignment.entity.AssignmentSubmission;
import com.mzc.backend.lms.domains.board.dto.response.AttachmentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private Boolean allowResubmission;
    private LocalDateTime resubmissionDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentResponseDto> attachments;

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
                .allowResubmission(submission.getAllowResubmission())
                .resubmissionDeadline(submission.getResubmissionDeadline())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .attachments(submission.getAttachments().stream()
                        .map(AttachmentResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
