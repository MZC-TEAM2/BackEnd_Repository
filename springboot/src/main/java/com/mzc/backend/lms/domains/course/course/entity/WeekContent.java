package com.mzc.backend.lms.domains.course.course.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

/**
 * 주차별 콘텐츠 엔티티
 * week_contents 테이블과 매핑
 */
@Entity
@Table(name = "week_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeekContent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    private CourseWeek week;

    @Column(name = "content_type", length = 20, nullable = false)
    private String contentType; // VIDEO, DOCUMENT, LINK

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content_url", length = 500, nullable = false)
    private String contentUrl;

    @Column(name = "duration", length = 10)
    private String duration; // 동영상 길이 (예: "45:23")

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder; // 표시 순서

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 콘텐츠 정보 수정
     */
    public void update(String contentType, String title, String contentUrl, String duration, Integer displayOrder) {
        if (contentType != null) {
            this.contentType = contentType;
        }
        if (title != null) {
            this.title = title;
        }
        if (contentUrl != null) {
            this.contentUrl = contentUrl;
        }
        if (duration != null) {
            this.duration = duration;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }
}

