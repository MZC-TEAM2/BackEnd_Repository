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

    @Column(name = "type_code", length = 20, unique = true, nullable = false)
    private int typeCode; // 강의 유형 코드 (예: LEC, LAB, SEMINAR, PROJECT, etc.)

    @Column(name = "type_name", length = 50, nullable = false)
    private int typeName; // 강의 유형 이름 (예: 전공필수, 전공선택, 교양필수, 교양선택)

    @Column(name = "category", length = 20, nullable = false)
    private int category; // 강의 유형 카테고리 (예: 전공, 교양)
}
