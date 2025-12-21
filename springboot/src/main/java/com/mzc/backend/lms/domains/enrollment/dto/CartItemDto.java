package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 장바구니 항목 DTO
 */
@Getter
@Builder
public class CartItemDto {
    private Long cartId;
    private CourseInfoDto course;
    private ProfessorDto professor;
    private List<ScheduleDto> schedule;
    private EnrollmentDto enrollment;
    private LocalDateTime addedAt;

    /**
     * 강의 기본 정보 DTO
     */
    @Getter
    @Builder
    public static class CourseInfoDto {
        private Long id;
        private String code;
        private String name;
        private String section;
        private Integer credits;
        private String courseType; // "전공필수", "전공선택", "교양필수", "교양선택"
        private Integer currentStudents;  // 수강인원 추가
        private Integer maxStudents;       // 전체 인원 추가
    }
}
