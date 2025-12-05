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
    @Index(name = "idx_professors_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Professor {

    @Id
    @Column(name = "professor_id", length = 20)
    private String professorId;  // 교번 (예: 20240101001) - PK이자 User.id와 동일

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "professor_id")
    private User user;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;  // 임용일자

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    private Professor(String professorId, User user, LocalDate appointmentDate) {
        this.professorId = professorId;
        this.user = user;
        this.appointmentDate = appointmentDate;
    }

    /**
     * 교수 생성
     */
    public static Professor create(String professorId, User user, LocalDate appointmentDate) {
        return Professor.builder()
                .professorId(professorId)
                .user(user)
                .appointmentDate(appointmentDate)
                .build();
    }

    /**
     * 교번 getter (기존 코드 호환용)
     */
    public String getProfessorNumber() {
        return this.professorId;
    }
}
