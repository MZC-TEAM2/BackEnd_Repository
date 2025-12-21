package com.mzc.backend.lms.domains.academy.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 기간 타입 엔티티
 * period_types 테이블과 매핑
 */
@Entity
@Table(name = "period_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PeriodType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_code", length = 20, unique = true, nullable = false)
    private String typeCode; // ENROLLMENT, COURSE_REGISTRATION, ADJUSTMENT, CANCELLATION

    @Column(name = "type_name", length = 50, nullable = false)
    private String typeName; // 수강신청, 강의등록, 정정, 수강철회

    @Column(name = "description", length = 200)
    private String description;
}

