package com.mzc.backend.lms.domains.user.professor.entity;

import com.mzc.backend.lms.domains.user.organization.entity.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 교수-학과 소속 관계 엔티티
 * professor_departments 테이블과 매핑
 */
@Entity
@Table(name = "professor_departments",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"professor_id", "department_id", "start_date"})
       },
       indexes = {
           @Index(name = "idx_professor_departments_professor_id", columnList = "professor_id"),
           @Index(name = "idx_professor_departments_department_id", columnList = "department_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfessorDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "is_primary")
    private Boolean isPrimary = true;  // 주 소속 여부

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;  // 소속 시작일

    @Column(name = "end_date")
    private LocalDate endDate;  // 소속 종료일 (null이면 현재 소속)

    @Builder
    private ProfessorDepartment(Professor professor, Department department, Boolean isPrimary, LocalDate startDate, LocalDate endDate) {
        this.professor = professor;
        this.department = department;
        this.isPrimary = isPrimary != null ? isPrimary : true;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 교수-학과 관계 생성
     */
    public static ProfessorDepartment create(Professor professor, Department department, Boolean isPrimary, LocalDate startDate) {
        return ProfessorDepartment.builder()
                .professor(professor)
                .department(department)
                .isPrimary(isPrimary)
                .startDate(startDate)
                .build();
    }

    /**
     * 소속 종료
     */
    public void terminate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * 현재 소속 여부 확인
     */
    public boolean isActive() {
        return this.endDate == null;
    }
}
