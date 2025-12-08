package com.mzc.backend.lms.domains.user.student.entity;

import com.mzc.backend.lms.domains.user.organization.entity.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 학생-학과 소속 관계 엔티티
 * student_departments 테이블과 매핑
 */
@Entity
@Table(name = "student_departments",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"student_id", "department_id"})
       },
       indexes = {
           @Index(name = "idx_student_departments_student_id", columnList = "student_id"),
           @Index(name = "idx_student_departments_department_id", columnList = "department_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "is_primary")
    private Boolean isPrimary = true;  // 주전공 여부 (true: 주전공, false: 복수/부전공)

    @Column(name = "enrolled_date", nullable = false)
    private LocalDate enrolledDate;  // 학과 등록일

    @Builder
    private StudentDepartment(Student student, Department department, Boolean isPrimary, LocalDate enrolledDate) {
        this.student = student;
        this.department = department;
        this.isPrimary = isPrimary != null ? isPrimary : true;
        this.enrolledDate = enrolledDate;
    }

    /**
     * 학생-학과 관계 생성
     */
    public static StudentDepartment create(Student student, Department department, Boolean isPrimary, LocalDate enrolledDate) {
        return StudentDepartment.builder()
                .student(student)
                .department(department)
                .isPrimary(isPrimary)
                .enrolledDate(enrolledDate)
                .build();
    }

    /**
     * 주전공으로 변경
     */
    public void setPrimary() {
        this.isPrimary = true;
    }

    /**
     * 부/복수전공으로 변경
     */
    public void setSecondary() {
        this.isPrimary = false;
    }
}
