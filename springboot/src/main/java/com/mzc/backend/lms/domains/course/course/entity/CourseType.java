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

    /**
     * typeCode를 문자열로 반환 (API 호환용)
     */
    public String getTypeCodeString() {
        return switch (typeCode) {
            case 1 -> "MAJOR_REQ";
            case 2 -> "MAJOR_ELEC";
            case 3 -> "GEN_REQ";
            case 4 -> "GEN_ELEC";
            default -> "UNKNOWN";
        };
    }

    /**
     * 이수구분 이름 반환
     */
    public String getTypeName() {
        return switch (typeCode) {
            case 1 -> "전공필수";
            case 2 -> "전공선택";
            case 3 -> "교양필수";
            case 4 -> "교양선택";
            default -> "미지정";
        };
    }

    /**
     * UI 표시용 색상
     */
    public String getColor() {
        return switch (typeCode) {
            case 1 -> "#FFB4C8";  // 전공필수 - 분홍
            case 2 -> "#B4D7FF";  // 전공선택 - 파랑
            case 3 -> "#FFD9B4";  // 교양필수 - 주황
            case 4 -> "#C8E6C9";  // 교양선택 - 초록
            default -> "#E0E0E0"; // 회색
        };
    }

    /**
     * 문자열 코드로 typeCode 숫자 변환
     */
    public static int parseTypeCode(String typeCodeString) {
        return switch (typeCodeString) {
            case "MAJOR_REQ" -> 1;
            case "MAJOR_ELEC" -> 2;
            case "GEN_REQ" -> 3;
            case "GEN_ELEC" -> 4;
            default -> throw new IllegalArgumentException("Invalid course type code: " + typeCodeString);
        };
    }
}
