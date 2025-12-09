package com.mzc.backend.lms.domains.notification.dto;

import com.mzc.backend.lms.domains.notification.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Getter
@Builder
public class NotificationResponseDto {

    private Long id;
    private String typeCode;
    private String typeName;
    private String category;
    private Long senderId;
    private String senderName;
    private String relatedEntityType;
    private Long relatedEntityId;
    private Long courseId;
    private String title;
    private String message;
    private String actionUrl;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    /**
     * Entity to DTO 변환
     */
    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .typeCode(notification.getNotificationType().getTypeCode())
                .typeName(notification.getNotificationType().getTypeName())
                .category(notification.getNotificationType().getCategory())
                .senderId(notification.getSender() != null ? notification.getSender().getId() : null)
                .senderName(getSenderName(notification))
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .courseId(notification.getCourseId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .actionUrl(notification.getActionUrl())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * 발신자 이름 조회
     */
    private static String getSenderName(Notification notification) {
        if (notification.getSender() == null) {
            return null;
        }
        if (notification.getSender().getUserProfile() == null) {
            return null;
        }
        return notification.getSender().getUserProfile().getName();
    }
}
