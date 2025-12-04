package com.mzc.backend.lms.domains.user.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 유형 엔티티
 * user_types 테이블과 매핑
 */
@Entity
@Table(name = "user_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type_code", length = 20, unique = true, nullable = false)
    private String typeCode;  // STUDENT, PROFESSOR

    @Column(name = "type_name", length = 50, nullable = false)
    private String typeName;  // 학생, 교수

    /**
     * 유형 코드 열거형
     */
    public enum TypeCode {
        STUDENT("STUDENT", "학생"),
        PROFESSOR("PROFESSOR", "교수");

        private final String code;
        private final String name;

        TypeCode(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }
}