package com.mzc.backend.lms.domains.attendance.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 주차별 출석 현황 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekAttendanceDto {

    private Long weekId;
    private Integer weekNumber;
    private String weekTitle;
    private Boolean isCompleted;
    private Integer completedVideoCount;
    private Integer totalVideoCount;
    private LocalDateTime completedAt;

    /**
     * 진행률 계산 (0 ~ 100)
     */
    public Integer getProgressPercentage() {
        if (totalVideoCount == null || totalVideoCount == 0) {
            return 100;
        }
        return (int) ((completedVideoCount * 100.0) / totalVideoCount);
    }
}
