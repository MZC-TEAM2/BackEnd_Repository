package com.mzc.backend.lms.domains.user.organization.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 학과 엔티티
 * departments 테이블과 매핑
 */
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_departments_department_code", columnList = "department_code"),
    @Index(name = "idx_departments_college_id", columnList = "college_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "department_code", length = 20, unique = true, nullable = false)
    private String departmentCode;  // 학과 코드 (예: CSE, ECE)

    @Column(name = "department_name", length = 100, nullable = false)
    private String departmentName;  // 학과명 (예: 컴퓨터공학과)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;  // 소속 단과대학

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    private Department(String departmentCode, String departmentName, College college) {
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.college = college;
    }

    /**
     * 학과 생성
     */
    public static Department create(String departmentCode, String departmentName, College college) {
        return Department.builder()
                .departmentCode(departmentCode)
                .departmentName(departmentName)
                .college(college)
                .build();
    }
}