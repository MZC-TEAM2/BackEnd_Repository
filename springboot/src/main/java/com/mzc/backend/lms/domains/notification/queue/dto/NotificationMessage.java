package com.mzc.backend.lms.domains.notification.queue.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Redis 큐에서 사용하는 알림 메시지 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer typeId;
    private Long senderId;
    private Long recipientId;
    private Long courseId;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String title;
    private String message;
    private String actionUrl;
    private LocalDateTime createdAt;

    @Builder
    private NotificationMessage(Integer typeId, Long senderId, Long recipientId,
                                 Long courseId, String relatedEntityType, Long relatedEntityId,
                                 String title, String message, String actionUrl) {
        this.typeId = typeId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.courseId = courseId;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.title = title;
        this.message = message;
        this.actionUrl = actionUrl;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 단순 알림 메시지 생성
     */
    public static NotificationMessage of(Integer typeId, Long senderId, Long recipientId, String message) {
        return NotificationMessage.builder()
                .typeId(typeId)
                .senderId(senderId)
                .recipientId(recipientId)
                .message(message)
                .build();
    }

    /**
     * 강의 관련 알림 메시지 생성
     */
    public static NotificationMessage forCourse(Integer typeId, Long senderId, Long recipientId,
                                                  Long courseId, String title, String message) {
        return NotificationMessage.builder()
                .typeId(typeId)
                .senderId(senderId)
                .recipientId(recipientId)
                .courseId(courseId)
                .title(title)
                .message(message)
                .build();
    }

    /**
     * 관련 엔티티 정보가 포함된 알림 메시지 생성
     */
    public static NotificationMessage withRelatedEntity(Integer typeId, Long senderId, Long recipientId,
                                                          String relatedEntityType, Long relatedEntityId,
                                                          Long courseId, String title, String message,
                                                          String actionUrl) {
        return NotificationMessage.builder()
                .typeId(typeId)
                .senderId(senderId)
                .recipientId(recipientId)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .courseId(courseId)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .build();
    }
}
