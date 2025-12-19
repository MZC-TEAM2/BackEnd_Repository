package com.mzc.backend.lms.domains.dashboard.student.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 미제출 과제 응답 DTO
 */
@Getter
public class PendingAssignmentDto {

    private final Long assignmentId;
    private final Long postId;
    private final String title;
    private final Long courseId;
    private final String courseName;
    private final String subjectName;
    private final LocalDateTime dueDate;
    private final Boolean lateSubmissionAllowed;

    /**
     * JPQL 생성자
     */
    public PendingAssignmentDto(
            Long assignmentId,
            Long postId,
            String title,
            Long courseId,
            String courseName,
            String subjectName,
            LocalDateTime dueDate,
            Boolean lateSubmissionAllowed
    ) {
        this.assignmentId = assignmentId;
        this.postId = postId;
        this.title = title;
        this.courseId = courseId;
        this.courseName = courseName;
        this.subjectName = subjectName;
        this.dueDate = dueDate;
        this.lateSubmissionAllowed = lateSubmissionAllowed;
    }

    /**
     * 남은 일수 계산
     */
    public Long getDaysRemaining() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }
}
