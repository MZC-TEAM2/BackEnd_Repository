package com.mzc.backend.lms.domains.course.subject.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.user.organization.entity.Department;

import java.time.LocalDateTime;

/**
 * 과목 엔티티
 * subjects 테이블과 매핑
 */
@Getter
@Setter
@Table(name = "subjects")
@NoArgsConstructor
@Entity @Builder @AllArgsConstructor
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_code", length = 8, unique = true, nullable = false)
    private String subjectCode; // 과목 코드 (예: CSE101)

    @Column(name = "subject_name", length = 20, nullable = false)
    private String subjectName; // 과목 이름 (예: 컴퓨터 공학 개론)     

    @Column(name = "subject_description", length = 200, nullable = false)
    private String subjectDescription; // 과목 설명 (예: 컴퓨터 공학 개론은 컴퓨터 공학의 기초를 다루는 과목입니다.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Course> courses = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_type_id", nullable = false)
    private CourseType courseType;
    
    @Column(nullable = false)
    private Integer credits; // 1~4
    
    @Column(name = "theory_hours")
    private Integer theoryHours;
    
    @Column(name = "practice_hours")
    private Integer practiceHours;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 선수과목
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubjectPrerequisites> prerequisites = new ArrayList<>();

    // 편의 메서드
    public void addPrerequisite(Subject prerequisiteSubject, boolean isMandatory) {
        SubjectPrerequisites relation = SubjectPrerequisites.builder()
            .subject(this)
            .prerequisite(prerequisiteSubject)
            .isMandatory(isMandatory)
            .build();
        prerequisites.add(relation);
        relation.setSubject(this);
    }
    
    public void removePrerequisite(SubjectPrerequisites relation) {
        prerequisites.remove(relation);
        relation.setSubject(null);
        relation.setPrerequisite(null);
        relation.getSubject().getPrerequisites().remove(relation);
    }
}
