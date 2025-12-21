package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * 강의 상세 정보 DTO
 * CourseDto의 모든 정보 + Subject 상세 정보 + 주차별 계획 + 선수과목 + 학기 정보
 */
@Getter
@Builder
public class CourseDetailDto {
    // ==================== 기본 강의 정보 (CourseDto와 동일) ====================
    private Long id;
    private String courseCode;
    private String courseName;
    private String section;
    private ProfessorDto professor;
    private DepartmentDto department;
    private Integer credits;
    private CourseTypeDto courseType;
    private List<ScheduleDto> schedule;
    private String scheduleText;
    private EnrollmentInfoDto enrollment;
    
    // ==================== Subject 상세 정보 ====================
    private String subjectDescription;  // 과목 설명 (200자)
    private String description;          // 과목 상세 설명 (TEXT)
    private Integer theoryHours;         // 이론 시수
    private Integer practiceHours;      // 실습 시수
    
    // ==================== 주차별 강의 계획 ====================
    private List<WeekDto> weeks;
    
    // ==================== 선수과목 정보 ====================
    private List<PrerequisiteDto> prerequisites;
    
    // ==================== 학기 정보 ====================
    private AcademicTermDto academicTerm;
}
