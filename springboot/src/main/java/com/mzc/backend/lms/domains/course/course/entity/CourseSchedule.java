package com.mzc.backend.lms.domains.course.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Convert;

import java.time.DayOfWeek;
import java.time.LocalTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Setter;
import com.mzc.backend.lms.domains.course.course.converter.DayOfWeekConverter;

/*
  강의 스케줄 엔티티
  course_schedules 테이블과 매핑
*/

@Entity @Setter
@Table(name = "course_schedules")
@Getter @Builder @AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId; // 시간표 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "day_of_week", nullable = false)
    @Convert(converter = DayOfWeekConverter.class)
    private DayOfWeek dayOfWeek; 
    
    @Column(name = "start_time", nullable = false, columnDefinition = "TIME")
    private LocalTime startTime; // 시작 시간 (예: 09:00)

    @Column(name = "end_time", nullable = false, columnDefinition = "TIME")
    private LocalTime endTime; // 종료 시간 (예: 10:30)

    @Column(name = "schedule_room", length = 50, nullable = false)
    private String scheduleRoom; // 강의실
}
