package com.mzc.backend.lms.domains.board.assignment.dto.response;

import com.mzc.backend.lms.domains.board.assignment.entity.Assignment;
import com.mzc.backend.lms.domains.board.dto.response.PostResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 과제 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class AssignmentResponseDto {

    private Long id;
    private Long postId;
    private Long courseId;
    private LocalDateTime dueDate;
    private BigDecimal maxScore;
    private String submissionMethod;
    private Boolean lateSubmissionAllowed;
    private BigDecimal latePenaltyPercent;
    private Integer maxFileSizeMb;
    private String allowedFileTypes;
    private String instructions;
    private Long createdBy;
    private String createdByName;  // 작성자(교수) 이름
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PostResponseDto post; // 게시글 정보 포함

    /**
     * 작성자 이름 설정 (UserInfoCacheService를 통해 조회 후 설정)
     */
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public static AssignmentResponseDto from(Assignment assignment) {
        return AssignmentResponseDto.builder()
                .id(assignment.getId())
                .postId(assignment.getPost().getId())
                .courseId(assignment.getCourseId())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .submissionMethod(assignment.getSubmissionMethod())
                .lateSubmissionAllowed(assignment.getLateSubmissionAllowed())
                .latePenaltyPercent(assignment.getLatePenaltyPercent())
                .maxFileSizeMb(assignment.getMaxFileSizeMb())
                .allowedFileTypes(assignment.getAllowedFileTypes())
                .instructions(assignment.getInstructions())
                .createdBy(assignment.getCreatedBy())
                .createdByName(null)
                .updatedBy(assignment.getUpdatedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .post(PostResponseDto.from(assignment.getPost()))
                .build();
    }

    public static AssignmentResponseDto fromWithoutPost(Assignment assignment) {
        return AssignmentResponseDto.builder()
                .id(assignment.getId())
                .postId(assignment.getPost().getId())
                .courseId(assignment.getCourseId())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .submissionMethod(assignment.getSubmissionMethod())
                .lateSubmissionAllowed(assignment.getLateSubmissionAllowed())
                .latePenaltyPercent(assignment.getLatePenaltyPercent())
                .maxFileSizeMb(assignment.getMaxFileSizeMb())
                .allowedFileTypes(assignment.getAllowedFileTypes())
                .instructions(assignment.getInstructions())
                .createdBy(assignment.getCreatedBy())
                .createdByName(null)  // AssignmentService.enrichWithUserInfo()에서 설정됨
                .updatedBy(assignment.getUpdatedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
