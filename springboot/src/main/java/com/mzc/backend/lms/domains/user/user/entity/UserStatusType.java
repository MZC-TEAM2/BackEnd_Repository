package com.mzc.backend.lms.domains.user.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 상태 유형 엔티티
 * user_status_types 테이블과 매핑
 */
@Entity
@Table(name = "user_status_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatusType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "status_code", length = 20, unique = true, nullable = false)
    private String statusCode;  // ACTIVE, INACTIVE, SUSPENDED

    @Column(name = "status_name", length = 50, nullable = false)
    private String statusName;  // 활성, 비활성, 정지

    /**
     * 상태 코드 열거형
     */
    public enum StatusCode {
        ACTIVE("ACTIVE", "활성"),
        INACTIVE("INACTIVE", "비활성"),
        SUSPENDED("SUSPENDED", "정지");

        private final String code;
        private final String name;

        StatusCode(String code, String name) {
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