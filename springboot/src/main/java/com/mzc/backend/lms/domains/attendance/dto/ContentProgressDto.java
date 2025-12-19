package com.mzc.backend.lms.domains.attendance.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 콘텐츠별 진행 상황 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentProgressDto {

    private Long contentId;
    private String title;
    private String contentType;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
}
