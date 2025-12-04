package com.mzc.backend.lms.domains.user.professor.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 교수 엔티티
 * professors 테이블과 매핑
 */
@Entity
@Table(name = "professors", indexes = {
    @Index(name = "idx_professors_professor_number", columnList = "professor_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Professor {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "professor_number", length = 20, unique = true, nullable = false)
    private String professorNumber;  // 교번 (예: P2024001)

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;  // 임용일자

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    private Professor(User user, String professorNumber, LocalDate appointmentDate) {
        this.user = user;
        this.professorNumber = professorNumber;
        this.appointmentDate = appointmentDate;
        this.userId = user.getId();
    }

    /**
     * 교수 생성
     */
    public static Professor create(User user, String professorNumber, LocalDate appointmentDate) {
        return Professor.builder()
                .user(user)
                .professorNumber(professorNumber)
                .appointmentDate(appointmentDate)
                .build();
    }
}
