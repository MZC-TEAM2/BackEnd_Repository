package com.mzc.backend.lms.domains.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 타입 정의 엔티티
 * notification_types 테이블과 매핑
 */
@Entity
@Table(name = "notification_types", indexes = {
    @Index(name = "idx_notification_types_type_code", columnList = "type_code"),
    @Index(name = "idx_notification_types_category", columnList = "category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type_code", length = 50, unique = true, nullable = false)
    private String typeCode;

    @Column(name = "type_name", length = 100, nullable = false)
    private String typeName;

    @Column(name = "category", length = 30, nullable = false)
    private String category;

    @Column(name = "default_message_template", columnDefinition = "TEXT", nullable = false)
    private String defaultMessageTemplate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    private NotificationType(String typeCode, String typeName, String category,
                             String defaultMessageTemplate, Boolean isActive) {
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.category = category;
        this.defaultMessageTemplate = defaultMessageTemplate;
        this.isActive = isActive != null ? isActive : true;
    }

    /**
     * 알림 타입 생성 팩토리 메소드
     */
    public static NotificationType create(String typeCode, String typeName, String category,
                                          String defaultMessageTemplate) {
        return NotificationType.builder()
                .typeCode(typeCode)
                .typeName(typeName)
                .category(category)
                .defaultMessageTemplate(defaultMessageTemplate)
                .isActive(true)
                .build();
    }

    /**
     * 알림 타입 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 알림 타입 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 메시지 템플릿 변경
     */
    public void changeMessageTemplate(String newTemplate) {
        this.defaultMessageTemplate = newTemplate;
    }
}