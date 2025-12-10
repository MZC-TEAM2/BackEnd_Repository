package com.mzc.backend.lms.domains.course.course.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


/**
 * 강의 유형 엔티티
 * course_types 테이블과 매핑
 */

@Entity
@Table(name = "course_types")
@Getter @Builder @AllArgsConstructor
@NoArgsConstructor
public class CourseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_code", unique = true, nullable = false)
    private int typeCode; // 강의 유형 코드 (1: MAJOR_REQ, 2: MAJOR_ELEC, 3: GEN_REQ, 4: GEN_ELEC)

    @Column(name = "category", nullable = false)
    private int category; // 강의 유형 카테고리 (0: 전공, 1: 교양)
}
