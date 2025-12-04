package com.mzc.backend.lms.domains.user.organization.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 단과대학 엔티티
 * colleges 테이블과 매핑
 */
@Entity
@Table(name = "colleges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "college_code", length = 20, unique = true, nullable = false)
    private String collegeCode;  // 단과대학 코드 (예: ENG, BIZ)

    @Column(name = "college_name", length = 100, nullable = false)
    private String collegeName;  // 단과대학명 (예: 공과대학, 경영대학)

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL)
    private List<Department> departments = new ArrayList<>();

    @Builder
    private College(String collegeCode, String collegeName) {
        this.collegeCode = collegeCode;
        this.collegeName = collegeName;
    }

    /**
     * 단과대학 생성
     */
    public static College create(String collegeCode, String collegeName) {
        return College.builder()
                .collegeCode(collegeCode)
                .collegeName(collegeName)
                .build();
    }
}