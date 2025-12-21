package com.mzc.backend.lms.domains.course.course.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 강의 개설 요청 DTO (API 명세서 기준)
 * 방법 A: subjectId 사용 (기존 과목 선택)
 * 방법 B: subject 사용 (새 과목 생성)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequestDto {
    @JsonProperty("enrollmentPeriodId")
    private Long enrollmentPeriodId;  // 수강신청 기간 ID (필수)
    
    // 방법 A: 기존 과목 선택
    @JsonProperty("subjectId")
    private Long subjectId;           // 과목 ID (subjectId 또는 subject 중 하나 필수)
    
    // 방법 B: 새 과목 생성
    @JsonProperty("subject")
    private SubjectRequestDto subject;  // 새 과목 정보 (subjectId 또는 subject 중 하나 필수)
    
    @JsonProperty("section")
    private String section;           // 분반 (필수, 예: "01")
    
    @JsonProperty("maxStudents")
    private Integer maxStudents;      // 최대 수강생 수 (필수)
    
    @JsonProperty("description")
    private String description;       // 강의 설명 (선택, 분반별)
    
    @JsonProperty("schedule")
    private List<ScheduleRequestDto> schedule;  // 시간표 (필수)
    
    @JsonProperty("syllabus")
    private SyllabusRequestDto syllabus;  // 강의계획서 (필수)
    
    @JsonProperty("totalWeeks")
    private Integer totalWeeks;       // 총 주차 수 (필수)

    /**
     * 새 과목 정보 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectRequestDto {
        @JsonProperty("subjectCode")
        private String subjectCode;           // 과목 코드 (필수, 예: CS401)
        
        @JsonProperty("subjectName")
        private String subjectName;            // 과목명 (필수)
        
        @JsonProperty("englishName")
        private String englishName;            // 영문 과목명 (선택)
        
        @JsonProperty("credits")
        private Integer credits;               // 학점 (필수, 1-4)
        
        @JsonProperty("courseType")
        private String courseType;             // 이수구분 (필수, MAJOR_REQ/MAJOR_ELEC/GEN_REQ/GEN_ELEC)
        
        @JsonProperty("departmentId")
        private Long departmentId;             // 학과 ID (필수)
        
        @JsonProperty("description")
        private String description;            // 과목 설명 (선택)
        
        @JsonProperty("prerequisiteSubjectIds")
        private List<Long> prerequisiteSubjectIds;  // 선수과목 ID 배열 (선택)
    }
}

